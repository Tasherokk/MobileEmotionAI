package com.example.emotionsai.ui.hr.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.emotionsai.di.ServiceLocator

class HrAnalyticsFragment : Fragment() {

    private val vm: HrAnalyticsViewModel by viewModels {
        ServiceLocator.hrAnalyticsVMFactory(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HrAnalyticsScreen(vm)
                }
            }
        }

    override fun onResume() {
        super.onResume()
        vm.init()
    }
}
