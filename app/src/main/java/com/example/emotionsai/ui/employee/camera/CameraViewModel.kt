package com.example.emotionsai.ui.employee.camera

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.Event
import com.example.emotionsai.data.remote.FeedbackResponse
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
    private val referenceRepo: ReferenceRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<CameraUiState>(CameraUiState.Idle)
    val uiState: LiveData<CameraUiState> = _uiState

    private val _events = MutableLiveData<List<Event>>(emptyList())
    val events: LiveData<List<Event>> = _events

    private val _selectedEvent = MutableLiveData<Event?>(null)
    val selectedEvent: LiveData<Event?> = _selectedEvent

    init {
        loadActiveEvents()
    }

    private fun loadActiveEvents() {
        viewModelScope.launch {
            when (val result = referenceRepo.getEvents(active = true)) {
                is Result.Ok -> _events.value = result.value
                is Result.Err -> {
                    // Не критично, можно продолжать без событий
                }
            }
        }
    }

    fun selectEvent(event: Event?) {
        _selectedEvent.value = event
    }

    fun submitPhoto(photoFile: File) {
        _uiState.value = CameraUiState.Loading

        viewModelScope.launch {
            when (val result = feedbackRepo.submitFeedback(photoFile, _selectedEvent.value?.id)) {
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
