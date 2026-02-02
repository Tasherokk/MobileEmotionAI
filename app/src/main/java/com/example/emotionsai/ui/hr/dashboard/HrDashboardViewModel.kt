package com.example.emotionsai.ui.hr.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class HrDashboardUiState {
    object Loading : HrDashboardUiState()
    data class Success(
        val overview: FeedbackRepository
    ) : HrDashboardUiState()
    data class Error(val message: String) : HrDashboardUiState()
}

class HrDashboardViewModel(
    private val hrStatsRepo: FeedbackRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<HrDashboardUiState>(HrDashboardUiState.Loading)
    val uiState: LiveData<HrDashboardUiState> = _uiState

    private val _selectedPeriod = MutableLiveData(Period.WEEK)
    val selectedPeriod: LiveData<Period> = _selectedPeriod

    // Фильтры аналитики (HR)
    private val _selectedEventId = MutableLiveData<Int?>(null)
    val selectedEventId: LiveData<Int?> = _selectedEventId

    private val _selectedDepartment = MutableLiveData<String?>(null)
    val selectedDepartment: LiveData<String?> = _selectedDepartment

    private val _groupBy = MutableLiveData(GroupBy.DAY)
    val groupBy: LiveData<GroupBy> = _groupBy

    init {
        loadData()
    }

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
        loadData()
    }

    fun selectEvent(eventId: Int?) {
        _selectedEventId.value = eventId
        loadData()
    }

    fun selectDepartment(department: String?) {
        _selectedDepartment.value = department
        loadData()
    }

    fun selectGroupBy(groupBy: GroupBy) {
        _groupBy.value = groupBy
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

            val eventId = _selectedEventId.value
            val department = _selectedDepartment.value
            val groupBy = (_groupBy.value ?: GroupBy.DAY).apiValue

            // ✅ 1) overview (расширенный, мок если бэк не готов)
//            val overviewResult = hrStatsRepo.getOverviewEx(
//                from = from,
//                to = to,
//                eventId = eventId,
//                department = department
//            )
//
//            // ✅ 2) timeline
//            val timelineResult = hrStatsRepo.getTimeline(
//                from = from,
//                to = to,
//                groupBy = groupBy,
//                eventId = eventId,
//                department = department
//            )
//
//            val byUserResult = hrStatsRepo.getByUser(
//                from = from, to = to, limit = 10, eventId = eventId, department = department
//            )

//            when {
//                overviewResult is Result.Ok && timelineResult is Result.Ok && byUserResult is Result.Ok -> {
//                    _uiState.value = HrDashboardUiState.Success(
//                        overview = overviewResult.value,
//                        timeline = timelineResult.value,
//                        byUser = byUserResult.value
//                    )
//                }
//                overviewResult is Result.Err -> _uiState.value = HrDashboardUiState.Error(overviewResult.message)
//                timelineResult is Result.Err -> _uiState.value = HrDashboardUiState.Error(timelineResult.message)
//                byUserResult is Result.Err -> _uiState.value = HrDashboardUiState.Error(byUserResult.message)
//                else -> _uiState.value = HrDashboardUiState.Error("Failed to load HR dashboard")
//            }

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

    enum class Period { WEEK, MONTH, ALL }

    enum class GroupBy(val apiValue: String) {
        DAY("day"),
        WEEK("week"),
        MONTH("month")
    }
}
