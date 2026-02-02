package com.example.emotionsai.ui.hr.events

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
                val action = HrEventsFragmentDirections.actionMyEventsFragmentToCreateEventFragment(
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
            findNavController().navigate(R.id.action_myEventsFragment_to_createEventFragment)
        }


        vm.events.observe(viewLifecycleOwner) { adapter.submitList(it) }
        vm.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank())
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
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
}
