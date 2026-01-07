package com.example.emotionsai.ui.hr.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.HrOverviewResponse
import com.example.emotionsai.data.remote.HrByUserResponse
import com.example.emotionsai.data.repo.HrStatsRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class HrDashboardUiState {
    object Loading : HrDashboardUiState()
    data class Success(val overview: HrOverviewResponse, val userStats: HrByUserResponse) : HrDashboardUiState()
    data class Error(val message: String) : HrDashboardUiState()
}

class HrDashboardViewModel(
    private val hrStatsRepo: HrStatsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HrDashboardUiState>(HrDashboardUiState.Loading)
    val uiState: LiveData<HrDashboardUiState> = _uiState

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
        _uiState.value = HrDashboardUiState.Loading

        viewModelScope.launch {
            val period = _selectedPeriod.value ?: Period.WEEK
            val (from, to) = getPeriodDates(period)

            // Load overview and user stats
            val overviewResult = hrStatsRepo.getOverview(from, to, null, null)
            val userStatsResult = hrStatsRepo.getByUser(from, to, 10, null, null)

            when {
                overviewResult is Result.Ok && userStatsResult is Result.Ok -> {
                    _uiState.value = HrDashboardUiState.Success(
                        overview = overviewResult.value,
                        userStats = userStatsResult.value
                    )
                }
                overviewResult is Result.Err -> {
                    _uiState.value = HrDashboardUiState.Error(overviewResult.message)
                }
                userStatsResult is Result.Err -> {
                    _uiState.value = HrDashboardUiState.Error(userStatsResult.message)
                }
            }
        }
    }

    private fun getPeriodDates(period: Period): Pair<String?, String?> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        val to = dateFormat.format(calendar.time)

        when (period) {
            Period.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            Period.MONTH -> calendar.add(Calendar.MONTH, -1)
            Period.ALL -> return Pair(null, null)
        }

        val from = dateFormat.format(calendar.time)
        return Pair(from, to)
    }

    enum class Period {
        WEEK, MONTH, ALL
    }
}
