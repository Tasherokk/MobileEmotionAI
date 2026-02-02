package com.example.emotionsai.ui.hr.analytics

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.example.emotionsai.data.remote.Feedback
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HrAnalyticsViewModel(
    private val repo: FeedbackRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _data = MutableLiveData<List<Feedback>>(emptyList())
    val data: LiveData<List<Feedback>> = _data

    private val _events = MutableLiveData<List<HrEventDto>>(emptyList())
    val events: LiveData<List<HrEventDto>> = _events

    // --- Filters (Compose state) ---
    var selectedDepartments = mutableStateOf<List<Int>>(emptyList())
    var selectedEmotions = mutableStateOf<List<String>>(emptyList())
    var selectedEventId = mutableStateOf<Int?>(null)
    var selectedHasEvent = mutableStateOf<Boolean?>(null)

    var startDate = mutableStateOf("2020-01-01")
    var endDate = mutableStateOf("2035-01-01")

    private var reloadJob: Job? = null

    fun init() {
        loadEvents()
        loadAnalytics()
    }

    fun onFiltersChanged() {
        // чтобы не дергать бек 10 раз подряд при кликах
        reloadJob?.cancel()
        reloadJob = viewModelScope.launch {
            delay(250)
            loadAnalytics()
        }
    }

    fun loadEvents() {
        viewModelScope.launch {
            when (val res = repo.loadHrEvents()) {
                is Result.Ok -> _events.value = res.value
                is Result.Err -> { /* можно молча */ }
            }
        }
    }

    fun loadAnalytics() {
        _loading.value = true

        viewModelScope.launch {
            when (val res = repo.loadFeedbacks(
                start = startDate.value,
                end = endDate.value,
                departments = selectedDepartments.value,
                emotions = selectedEmotions.value,
                eventId = selectedEventId.value,
                hasEvent = selectedHasEvent.value
            )) {
                is Result.Ok -> {
                    _data.value = res.value
                    _error.value = null
                }
                is Result.Err -> {
                    _error.value = res.message
                }
            }
            _loading.value = false
        }
    }
}
