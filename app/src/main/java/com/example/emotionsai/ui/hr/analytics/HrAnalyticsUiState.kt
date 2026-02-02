package com.example.emotionsai.ui.hr.analytics

import com.example.emotionsai.data.remote.Feedback

data class HrAnalyticsUiState(
    val loading: Boolean = false,
    val error: String? = null,

    val startDate: String = "2025-01-01",
    val endDate: String = "2030-01-01",
    val selectedEmotions: List<String> = emptyList(),
    val selectedDepartments: List<Int> = emptyList(),
    val selectedEventId: Int? = null,
    val hasEvent: Boolean? = null,

    val feedbacks: List<Feedback> = emptyList(),

    val emotionPie: Map<String, Float> = emptyMap(),
    val departmentMatrix: Map<String, Map<String, Int>> = emptyMap(),
    val kpis: Map<String, String> = emptyMap()
)
