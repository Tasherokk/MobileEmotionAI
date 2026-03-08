package com.example.emotionsai.ui.hr.requests

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.data.remote.HrRequestItemDto
import com.example.emotionsai.databinding.FragmentHrRequestsBinding
import com.example.emotionsai.di.ServiceLocator
import com.google.android.material.tabs.TabLayout

class HrRequestsFragment : Fragment(R.layout.fragment_hr_requests) {

    private var _vb: FragmentHrRequestsBinding? = null
    private val vb get() = _vb!!

    private val vm: HrRequestsViewModel by viewModels {
        ServiceLocator.hrRequestsVMFactory(requireContext())
    }

    private lateinit var adapter: HrRequestsAdapter

    private var allItems: List<HrRequestItemDto> = emptyList()
    private var selectedStatus: String = "OPEN"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentHrRequestsBinding.bind(view)

        adapter = HrRequestsAdapter { item ->
            val action = HrRequestsFragmentDirections
                .actionHrRequestsFragmentToHrRequestDetailsFragment(item.id)
            findNavController().navigate(action)
        }

        vb.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        vb.rvRequests.adapter = adapter

        setupTabs()

        vb.swipeRefresh.setOnRefreshListener { vm.load() }

        vb.btnSort.setOnClickListener {
            vm.toggleSortOrder()
        }

        vm.loading.observe(viewLifecycleOwner) {
            vb.progress.visibility = if (it) View.VISIBLE else View.GONE
            vb.swipeRefresh.isRefreshing = false
        }

        vm.error.observe(viewLifecycleOwner) {
            if (!it.isNullOrBlank()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        vm.items.observe(viewLifecycleOwner) { list ->
            allItems = list ?: emptyList()
            applyFilter()
        }
    }

    private fun setupTabs() {
        val tabs = vb.tabsStatus

        if (tabs.tabCount == 0) {
            tabs.addTab(tabs.newTab().setText("OPEN").setTag("OPEN"), true)
            tabs.addTab(tabs.newTab().setText("IN PROGRESS").setTag("IN_PROGRESS"))
            tabs.addTab(tabs.newTab().setText("CLOSED").setTag("CLOSED"))
        }

        selectedStatus = (tabs.getTabAt(tabs.selectedTabPosition)?.tag as? String) ?: "OPEN"

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedStatus = (tab.tag as? String) ?: "OPEN"
                applyFilter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun applyFilter() {
        val filtered = allItems.filter { it.status == selectedStatus }
        adapter.submitList(filtered)
        
        val isEmpty = filtered.isEmpty()
        vb.stateContainer.visibility = if (isEmpty) View.VISIBLE else View.GONE
        vb.tvStateIcon.visibility = if (isEmpty) View.VISIBLE else View.GONE
        vb.tvStateHint.visibility = if (isEmpty) View.VISIBLE else View.GONE
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
