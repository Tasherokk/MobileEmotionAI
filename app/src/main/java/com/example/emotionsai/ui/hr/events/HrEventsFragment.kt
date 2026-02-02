package com.example.emotionsai.ui.hr.events

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentHrEventsBinding
import com.example.emotionsai.di.ServiceLocator
import com.google.android.material.datepicker.MaterialDatePicker
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class HrEventsFragment : Fragment(R.layout.fragment_hr_events) {

    private var _vb: FragmentHrEventsBinding? = null
    private val vb get() = _vb!!

    private val vm: HrEventsViewModel by viewModels {
        ServiceLocator.hrEventsVMFactory(requireContext())
    }

    private lateinit var adapter: HrEventsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentHrEventsBinding.bind(view)

        adapter = HrEventsAdapter(
            isActive = { vm.isActive(it) },
            onEdit = { event ->
                val action = HrEventsFragmentDirections.actionHrEventsFragmentToCreateEventFragment(
                    eventId = event.id,
                    eventTitle = event.title,
                    eventStartsAt = event.starts_at,
                    eventEndsAt = event.ends_at ?: ""
                )
                findNavController().navigate(action)

            },
            onDelete = { event ->
                if (!vm.isActive(event)) {
                    Toast.makeText(requireContext(), "Можно удалить только активный ивент", Toast.LENGTH_SHORT).show()
                } else {
                    vm.deleteEvent(event.id)
                }
            }
        )
        vb.rvEvents.layoutManager = LinearLayoutManager(requireContext())   // ✅ ОБЯЗАТЕЛЬНО
        vb.rvEvents.adapter = adapter

        vb.inputSearch.addTextChangedListener { vm.onSearch(it.toString()) }

        vb.btnCreateEvent.setOnClickListener {
            findNavController().navigate(R.id.action_hrEventsFragment_to_createEventFragment)
        }

        vb.btnFilters.setOnClickListener {
            // вариант: сначала даты, потом активность
            showFilterMenu()
        }

//        vb.btnActivity.setOnClickListener {
//            showActivityDialog()
//        }
//
//        vb.btnClearFilters.setOnClickListener {
//            vm.clearFilters()
//        }
        vm.events.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
        }
        vm.filter.observe(viewLifecycleOwner) { f ->
            vb.btnFilters.text = buildString {
                append("Фильтр: ")
                append(
                    when (f.activity) {
                        ActivityFilter.ALL -> "все"
                        ActivityFilter.UPCOMING -> "предстоящие"
                        ActivityFilter.ONGOING -> "идут"
                        ActivityFilter.PAST -> "завершенные"
                        ActivityFilter.EDITABLE -> "редактируемые"
                    }
                )
                if (f.from != null || f.to != null) {
                    append(" • ")
                    append("${f.from ?: "…"} - ${f.to ?: "…"}")
                }
            }
        }


        vm.loadEvents()
    }

    override fun onResume() {
        super.onResume()
        vm.loadEvents()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
    private fun showDateRangePicker() {
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите период")
            .build()

        picker.addOnPositiveButtonClickListener { range ->
            val from = range.first?.toLocalDate()
            val to = range.second?.toLocalDate()
            vm.setDateRange(from, to)
        }

        picker.show(parentFragmentManager, "dateRange")
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
    private fun showActivityDialog() {
        val items = arrayOf(
            "Все",
            "Предстоящие",
            "Идут сейчас",
            "Завершенные",
            "Можно редактировать"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Активность")
            .setItems(items) { _, which ->
                val mode = when (which) {
                    1 -> ActivityFilter.UPCOMING
                    2 -> ActivityFilter.ONGOING
                    3 -> ActivityFilter.PAST
                    4 -> ActivityFilter.EDITABLE
                    else -> ActivityFilter.ALL
                }
                vm.setActivity(mode)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showFilterMenu() {
        val items = arrayOf(
            "Фильтр по дате",
            "Фильтр по активности",
            "Сбросить фильтры"
        )

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Фильтры")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showDateRangePicker()
                    1 -> showActivityDialog()
                    2 -> vm.clearFilters()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

}
