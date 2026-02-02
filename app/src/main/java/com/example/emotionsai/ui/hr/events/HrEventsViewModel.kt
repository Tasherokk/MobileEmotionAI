package com.example.emotionsai.ui.hr.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneId

class HrEventsViewModel(
    private val repo: EventRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _all = MutableLiveData<List<HrEventDto>>(emptyList())
    val all: LiveData<List<HrEventDto>> = _all

    private val _events = MutableLiveData<List<HrEventDto>>(emptyList())
    val events: LiveData<List<HrEventDto>> = _events

    private var searchQuery: String = ""
    private var fromDate: String? = null // "yyyy-MM-dd"
    private var toDate: String? = null   // "yyyy-MM-dd"

    fun loadEvents() {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadHrEvents()) {
                is Result.Ok -> {
                    _all.value = res.value
                    applyFilters()
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun onSearch(q: String) {
        searchQuery = q.trim()
        applyFilters()
    }

    fun setDateFilter(from: String?, to: String?) {
        fromDate = from
        toDate = to
        applyFilters()
    }

    fun deleteEvent(id: Int) {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.deleteHrEvent(id)) {
                is Result.Ok -> loadEvents()
                is Result.Err -> {
                    _error.value = res.message
                    _loading.value = false
                }
            }
        }
    }

    fun isActive(e: HrEventDto): Boolean {
        return try {
            val now = OffsetDateTime.now(ZoneId.systemDefault())
            val start = OffsetDateTime.parse(e.starts_at)
            val end = e.ends_at?.let { OffsetDateTime.parse(it) }

            if (end == null) now.isAfter(start) else (now.isAfter(start) && now.isBefore(end))
        } catch (_: Exception) {
            false
        }
    }

    private fun applyFilters() {
        val base = _all.value ?: emptyList()

        val bySearch = if (searchQuery.isBlank()) base
        else base.filter { it.title.contains(searchQuery, ignoreCase = true) }

        // фронтовый фильтр по датам: сравниваем только по "yyyy-MM-dd" часть starts_at
        val byDate = bySearch.filter { ev ->
            val day = ev.starts_at.take(10) // "2026-02-01"
            val okFrom = fromDate?.let { day >= it } ?: true
            val okTo = toDate?.let { day <= it } ?: true
            okFrom && okTo
        }

        _events.value = byDate
    }
}
