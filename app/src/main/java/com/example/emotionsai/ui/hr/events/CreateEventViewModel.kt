package com.example.emotionsai.ui.hr.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.EmployeeDto
import com.example.emotionsai.data.remote.EventCreateRequest
import com.example.emotionsai.data.remote.HrEventDetailsDto
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch

class CreateEventViewModel(
    private val repo: EventRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _success = MutableLiveData<Boolean?>(null)
    val success: LiveData<Boolean?> = _success

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _employees = MutableLiveData<List<EmployeeDto>>()
    val employees: LiveData<List<EmployeeDto>> = _employees

    private val _eventDetails = MutableLiveData<HrEventDetailsDto?>()
    val eventDetails: LiveData<HrEventDetailsDto?> = _eventDetails

    fun loadEmployees() {
        viewModelScope.launch {
            when (val res = repo.getCompanyEmployees()) {
                is Result.Ok -> _employees.value = res.value
                is Result.Err -> _error.value = res.message
            }
        }
    }

    fun loadEventDetails(eventId: Int) {
        viewModelScope.launch {
            _loading.value = true
            when (val res = repo.getHrEventDetails(eventId)) {
                is Result.Ok -> _eventDetails.value = res.value
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun createEvent(
        title: String,
        startIso: String,
        endIso: String?,
        companyId: Int,
        participantIds: List<Int>?
    ) {
        _loading.value = true

        val req = EventCreateRequest(
            title = title,
            starts_at = startIso,
            ends_at = endIso,
            company = companyId,
            participants = participantIds
        )

        viewModelScope.launch {
            when (val res = repo.createHrEvent(req)) {
                is Result.Ok -> _success.value = true
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun updateEvent(
        eventId: Int,
        title: String,
        startIso: String,
        endIso: String?,
        companyId: Int,
        participantIds: List<Int>?
    ) {
        _loading.value = true

        val req = EventCreateRequest(
            title = title,
            starts_at = startIso,
            ends_at = endIso,
            company = companyId,
            participants = participantIds
        )

        viewModelScope.launch {
            when (val res = repo.updateHrEvent(eventId, req)) {
                is Result.Ok -> _success.value = true
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun successHandled() {
        _success.value = null
    }
}
