package com.example.emotionsai.ui.hr.requests

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.emotionsai.R
import com.example.emotionsai.databinding.FragmentHrRequestsBinding
import com.example.emotionsai.di.ServiceLocator

class HrRequestsFragment : Fragment(R.layout.fragment_hr_requests) {

    private var _vb: FragmentHrRequestsBinding? = null
    private val vb get() = _vb!!

    private val vm: HrRequestsViewModel by viewModels {
        ServiceLocator.hrRequestsVMFactory(requireContext())
    }

    private lateinit var adapter: HrRequestsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _vb = FragmentHrRequestsBinding.bind(view)

        adapter = HrRequestsAdapter { item ->
            val action = HrRequestsFragmentDirections
                .actionHrRequestsFragmentToHrRequestDetailsFragment(item.id)
            findNavController().navigate(action)
        }

        vb.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        vb.rvRequests.adapter = adapter

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

        vm.items.observe(viewLifecycleOwner) { adapter.submitList(it) }
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
