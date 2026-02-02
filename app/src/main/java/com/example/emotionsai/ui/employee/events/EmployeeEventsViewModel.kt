package com.example.emotionsai.ui.employee.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.EmployeeEventDto
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch

class EmployeeEventsViewModel(
    private val repo: EventRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _events = MutableLiveData<List<EmployeeEventDto>>(emptyList())
    val events: LiveData<List<EmployeeEventDto>> = _events

    fun load() {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadMyEvents()) {
                is Result.Ok -> {
                    _events.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }
}
