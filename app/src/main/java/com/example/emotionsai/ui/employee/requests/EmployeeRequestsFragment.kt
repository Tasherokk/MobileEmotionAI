package com.example.emotionsai.ui.employee.requests

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentEmployeeRequestsBinding
import com.example.emotionsai.di.ServiceLocator

class EmployeeRequestsFragment : Fragment(R.layout.fragment_employee_requests) {

    private var _vb: FragmentEmployeeRequestsBinding? = null
    private val vb get() = _vb!!

    private val vm: EmployeeRequestsViewModel by viewModels {
        ServiceLocator.employeeRequestsVMFactory(requireContext())
    }

    private lateinit var adapter: EmployeeRequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentEmployeeRequestsBinding.bind(view)

        adapter = EmployeeRequestsAdapter { item ->
            val action = EmployeeRequestsFragmentDirections
                .actionEmployeeRequestsFragmentToRequestChatFragment(item.id)
            findNavController().navigate(action)
        }

        vb.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        vb.rvRequests.adapter = adapter

        vb.swipeRefresh.setOnRefreshListener { vm.load() }
        vb.btnCreate.setOnClickListener {
            findNavController().navigate(R.id.action_employeeRequestsFragment_to_createRequestFragment)
        }

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
            vb.swipeRefresh.isRefreshing = false
        }

        vm.error.observe(viewLifecycleOwner) {
            vb.tvError.visibility = if (it.isNullOrBlank()) View.GONE else View.VISIBLE
            vb.tvError.text = it ?: ""
            if (!it.isNullOrBlank()) Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            vb.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
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
