package com.example.emotionsai.ui.hr.events

import java.time.LocalDate

data class EventsFilterState(
    val from: LocalDate? = null,
    val to: LocalDate? = null,
    val activity: ActivityFilter = ActivityFilter.ALL
)