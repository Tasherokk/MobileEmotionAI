package com.example.emotionsai.ui.hr.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.time.LocalDate
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
    private val _filter = MutableLiveData(EventsFilterState())
    val filter: LiveData<EventsFilterState> = _filter
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

            when {
                end != null -> now.isBefore(end)          // будущие + текущие (пока не конец)
                else -> now.isBefore(start) || now.isAfter(start) // если end нет — всегда можно (или уточни правило)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun setDateRange(from: LocalDate?, to: LocalDate?) {
        _filter.value = (_filter.value ?: EventsFilterState()).copy(from = from, to = to)
        applyFilters()
    }

    fun setActivity(activity: ActivityFilter) {
        _filter.value = (_filter.value ?: EventsFilterState()).copy(activity = activity)
        applyFilters()
    }

    fun clearFilters() {
        _filter.value = EventsFilterState()
        applyFilters()
    }

    private fun applyFilters() {
        val list = _all.value.orEmpty()
        val f = _filter.value ?: EventsFilterState()

        val filtered = list
            .filter { e -> matchesDateRange(e, f.from, f.to) }
            .filter { e -> matchesActivity(e, f.activity) }
            .sortedBy { e -> safeStart(e) } // удобно: по дате старта

        _events.value = filtered
    }

    private fun matchesDateRange(e: HrEventDto, from: LocalDate?, to: LocalDate?): Boolean {
        if (from == null && to == null) return true

        val start = safeStart(e)?.toLocalDate() ?: return false
        val end = safeEnd(e)?.toLocalDate() ?: start

        // пересечение диапазонов [start..end] и [from..to]
        val fromOk = from == null || !end.isBefore(from)
        val toOk = to == null || !start.isAfter(to)
        return fromOk && toOk
    }

    private fun matchesActivity(e: HrEventDto, mode: ActivityFilter): Boolean {
        if (mode == ActivityFilter.ALL) return true

        val now = OffsetDateTime.now(ZoneId.systemDefault())
        val start = safeStart(e) ?: return false
        val end = safeEnd(e)

        val upcoming = now.isBefore(start)
        val ongoing = if (end != null) now.isAfter(start) && now.isBefore(end) else now.isAfter(start)
        val past = if (end != null) now.isAfter(end) else false

        val editable = if (end != null) now.isBefore(end) else true // будущие+текущие, если end нет — можно всегда

        return when (mode) {
            ActivityFilter.UPCOMING -> upcoming
            ActivityFilter.ONGOING -> ongoing
            ActivityFilter.PAST -> past
            ActivityFilter.EDITABLE -> editable
            else -> true
        }
    }

    private fun safeStart(e: HrEventDto): OffsetDateTime? =
        runCatching { OffsetDateTime.parse(e.starts_at) }.getOrNull()

    private fun safeEnd(e: HrEventDto): OffsetDateTime? =
        runCatching { e.ends_at?.let { OffsetDateTime.parse(it) } }.getOrNull()
}
