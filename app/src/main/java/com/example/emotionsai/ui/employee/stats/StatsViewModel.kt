package com.example.emotionsai.ui.employee.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.FeedbackResponse
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class StatsUiState {
    object Loading : StatsUiState()
    data class Success(val stats: FeedbackResponse) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

class StatsViewModel(
    private val feedbackRepo: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<StatsUiState>(StatsUiState.Loading)
    val uiState: LiveData<StatsUiState> = _uiState

    private val _selectedPeriod = MutableLiveData<Period>(Period.WEEK)
    val selectedPeriod: LiveData<Period> = _selectedPeriod

    init {
        loadData()
    }

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        _uiState.value = StatsUiState.Loading

        viewModelScope.launch {
//            val period = _selectedPeriod.value ?: Period.WEEK
////            val (from, to) = getPeriodDates(period)
//
//            // Load stats and history in parallel
//
//            when {
//                statsResult is Result.Ok && historyResult is Result.Ok -> {
//                    _uiState.value = StatsUiState.Success(
//                        stats = statsResult.value,
//                        history = historyResult.value.results
//                    )
//                }
//                statsResult is Result.Err -> {
//                    _uiState.value = StatsUiState.Error(statsResult.message)
//                }
//                historyResult is Result.Err -> {
//                    _uiState.value = StatsUiState.Error(historyResult.message)
//                }
//            }
        }
    }

    private fun getPeriodDates(period: Period): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val to = dateFormat.format(calendar.time)

        when (period) {
            Period.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            Period.MONTH -> calendar.add(Calendar.MONTH, -1)
            Period.ALL -> return Pair("", "") // No filter
        }

        val from = dateFormat.format(calendar.time)
        return Pair(from, to)
    }

    enum class Period {
        WEEK, MONTH, ALL
    }
}
