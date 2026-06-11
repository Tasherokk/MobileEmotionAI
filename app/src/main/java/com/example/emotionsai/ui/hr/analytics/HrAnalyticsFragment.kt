package com.example.emotionsai.ui.hr.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
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
                val dark = isSystemInDarkTheme()
                MaterialTheme(
                    colorScheme = if (dark) darkColorScheme(
                        primary = Color(0xFF818CF8),
                        onPrimary = Color.White,
                        secondary = Color(0xFFF472B6),
                        surface = Color(0xFF1E293B),
                        background = Color(0xFF0F172A),
                        onSurface = Color(0xFFE2E8F0),
                        error = Color(0xFFF87171),
                    ) else lightColorScheme(
                        primary = Color(0xFF6366F1),
                        onPrimary = Color.White,
                        secondary = Color(0xFFEC4899),
                        surface = Color.White,
                        background = Color(0xFFF8FAFC),
                        onSurface = Color(0xFF0F172A),
                        error = Color(0xFFEF4444),
                    )
                ) {
                    HrAnalyticsScreen(vm)
                }
            }
        }

    override fun onResume() {
        super.onResume()
        vm.init()
    }
}
