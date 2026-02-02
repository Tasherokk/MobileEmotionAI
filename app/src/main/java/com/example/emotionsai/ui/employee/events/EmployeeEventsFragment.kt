package com.example.emotionsai.ui.employee.events

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentEmployeeEventsBinding
import com.example.emotionsai.di.ServiceLocator

class EmployeeEventsFragment : Fragment(R.layout.fragment_employee_events) {

    private var _vb: FragmentEmployeeEventsBinding? = null
    private val vb get() = _vb!!

    private val vm: EmployeeEventsViewModel by viewModels {
        ServiceLocator.employeeEventsVMFactory(requireContext())
    }

    private lateinit var adapter: EmployeeEventsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentEmployeeEventsBinding.bind(view)

        adapter = EmployeeEventsAdapter { event ->
            // переход в CameraFragment
            // Вариант А (лучше): через safe args
            val action = EmployeeEventsFragmentDirections
                .actionEmployeeEventsFragmentToCameraFragment(event.id, event.title)
            findNavController().navigate(action)
        }

        vb.rvEmployeeEvents.layoutManager = LinearLayoutManager(requireContext())
        vb.rvEmployeeEvents.adapter = adapter

        vb.swipeRefresh.setOnRefreshListener { vm.load() }

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
            vb.swipeRefresh.isRefreshing = false
        }

        vm.error.observe(viewLifecycleOwner) {
            vb.tvError.visibility = if (it.isNullOrBlank()) View.GONE else View.VISIBLE
            vb.tvError.text = it ?: ""
            if (!it.isNullOrBlank()) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        vm.events.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
            Toast.makeText(requireContext(), "Events: ${list.size}", Toast.LENGTH_SHORT).show()

        }
        val handle = findNavController().currentBackStackEntry?.savedStateHandle

        handle?.getLiveData<Int?>("feedback_submitted_event_id")
            ?.observe(viewLifecycleOwner) { eventId ->
                if (eventId != null) {
                    vm.load() // перезагрузит с сервера и придет has_feedback=true
                }
                handle.remove<Int?>("feedback_submitted_event_id")
            }

        vm.load()

    }
    override fun onResume() {
        super.onResume()
        vm.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vb = null
    }
}
