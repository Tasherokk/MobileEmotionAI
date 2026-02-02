package com.example.emotionsai.ui.employee.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.EmployeeEventDto
import com.example.emotionsai.data.remote.FeedbackResponse
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.data.repo.ReferenceRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.io.File

sealed class CameraUiState {
    object Idle : CameraUiState()
    object Loading : CameraUiState()
    data class Success(val result: FeedbackResponse) : CameraUiState()
    data class Error(val message: String) : CameraUiState()
}

class CameraViewModel(
    private val feedbackRepo: FeedbackRepository,
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<CameraUiState>(CameraUiState.Idle)
    val uiState: LiveData<CameraUiState> = _uiState

    private val _events = MutableLiveData<List<EmployeeEventDto>>(emptyList())
    val events: LiveData<List<EmployeeEventDto>> = _events


    init {
        loadActiveEvents()
    }

    private fun loadActiveEvents() {
        viewModelScope.launch {
            when (val result = eventRepo.loadMyEvents()) {
                is Result.Ok -> _events.value = result.value
                is Result.Err -> {
                    // Не критично, можно продолжать без событий
                }
            }
        }
    }


    fun submitPhoto(photoFile: File, eventId: Int?) {
        _uiState.value = CameraUiState.Loading

        viewModelScope.launch {
            when (val result = feedbackRepo.submitFeedback(photoFile, eventId)) {
                is Result.Ok -> {
                    _uiState.value = CameraUiState.Success(result.value)
                }
                is Result.Err -> {
                    _uiState.value = CameraUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = CameraUiState.Idle
    }
}
