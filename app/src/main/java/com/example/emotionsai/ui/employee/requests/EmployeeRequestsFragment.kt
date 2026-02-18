package com.example.emotionsai.ui.employee.requests

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.EmployeeRequestItemDto
import com.example.emotionsai.databinding.FragmentEmployeeRequestsBinding
import com.example.emotionsai.di.ServiceLocator
import com.google.android.material.tabs.TabLayout
class EmployeeRequestsFragment : Fragment(R.layout.fragment_employee_requests) {

    private var _vb: FragmentEmployeeRequestsBinding? = null
    private val vb get() = _vb!!

    private var allItems: List<EmployeeRequestItemDto> = emptyList()
    private var selectedStatus: String = "OPEN" // default
    private val vm: EmployeeRequestsViewModel by viewModels {
        ServiceLocator.employeeRequestsVMFactory(requireContext())
    }

    private lateinit var adapter: EmployeeRequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentEmployeeRequestsBinding.bind(view)
// tabs
        vb.tabsStatus.apply {
            removeAllTabs()
            addTab(newTab().setText("OPEN").setTag("OPEN"))
            addTab(newTab().setText("IN PROGRESS").setTag("IN_PROGRESS"))
            addTab(newTab().setText("CLOSED").setTag("CLOSED"))
        }

        vb.tabsStatus.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedStatus = tab.tag as? String ?: "OPEN"
                renderFiltered()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                // можно скроллить вверх/обновлять — по желанию
            }
        })
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
            allItems = list
            renderFiltered()
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
    private fun renderFiltered() {
        val filtered = allItems.filter { it.status == selectedStatus }

        adapter.submitList(filtered)

        val isEmpty = filtered.isEmpty()
        vb.emptyContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        vb.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE

        // если хочешь, можно скрывать empty при наличии ошибки
        // vb.emptyContainer.isVisible = isEmpty && vb.errorContainer.visibility != View.VISIBLE
    }
}

