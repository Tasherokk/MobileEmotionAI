package com.example.emotionsai.ui.hr.events

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.EventCreateRequest
import com.example.emotionsai.databinding.FragmentCreateEventBinding
import com.example.emotionsai.di.ServiceLocator
import com.example.emotionsai.ui.profile.ProfileViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager

class CreateEventFragment : Fragment(R.layout.fragment_create_event) {

    private var _vb: FragmentCreateEventBinding? = null
    private val vb get() = _vb!!

    private val vm: CreateEventViewModel by viewModels {
        ServiceLocator.createEventVMFactory(requireContext())
    }

    private val profileVm: ProfileViewModel by viewModels()
    private var companyId: Int? = null

    private lateinit var adapter: EmployeeMultiAdapter

    private val args: CreateEventFragmentArgs by navArgs()
    private val isEditMode: Boolean get() = args.eventId != 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentCreateEventBinding.bind(view)

        adapter = EmployeeMultiAdapter { _, _ -> }
        vb.rvEmployees.layoutManager = LinearLayoutManager(requireContext())
        vb.rvEmployees.adapter = adapter

        setupProfile()
        setupDatePickers()
        setupObservers()

        prefillIfEdit()      // ✅ ДОБАВЬ

        vm.loadEmployees()
        setupCreateBtn()
    }
    private fun prefillIfEdit() {
        if (!isEditMode) return

        vb.inputTitle.setText(args.eventTitle)

        // приходит starts_at типа "2026-02-01T16:18:36+05:00"
        vb.inputStart.setText(args.eventStartsAt.take(10))

        val end = args.eventEndsAt
        vb.inputEnd.setText(if (end.isBlank()) "" else end.take(10))

        vb.btnCreate.text = "Save changes"
    }

    private fun setupProfile() {
        profileVm.me.observe(viewLifecycleOwner) { me ->
            if (me != null) {
                companyId = me.company
                vb.tvCompany.text = me.company_name
            }
        }
        profileVm.loadMe()
    }

    private fun setupDatePickers() {
        val dpStart = MaterialDatePicker.Builder.datePicker().build()
        vb.inputStart.setOnClickListener {
            dpStart.addOnPositiveButtonClickListener {
                vb.inputStart.setText(formatDate(it))
            }
            dpStart.show(parentFragmentManager, "startDate")
        }

        val dpEnd = MaterialDatePicker.Builder.datePicker().build()
        vb.inputEnd.setOnClickListener {
            dpEnd.addOnPositiveButtonClickListener {
                vb.inputEnd.setText(formatDate(it))
            }
            dpEnd.show(parentFragmentManager, "endDate")
        }
    }

    private fun setupCreateBtn() {
        vb.btnCreate.setOnClickListener {
            val title = vb.inputTitle.text.toString()
            val start = vb.inputStart.text.toString()
            val end = vb.inputEnd.text.toString().ifBlank { null }
            val parts = adapter.getSelected()

            if (companyId == null) {
                Toast.makeText(requireContext(), "No company", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                vm.updateEvent(
                    eventId = args.eventId,
                    title = title,
                    start = start,
                    end = end,
                    companyId = companyId!!,
                    participantIds = parts
                )
            } else {
                vm.createEvent(title, start, end, companyId!!, parts)
            }
        }
    }


    private fun setupObservers() {
        vm.employees.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.success.observe(viewLifecycleOwner) { ok ->
            if (ok == true) {
                vm.successHandled()
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) "Event updated!" else "Event created!",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack()
            }
        }


        vm.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty())
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
        }
    }

    private fun formatDate(ts: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(ts))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
