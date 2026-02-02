package com.example.emotionsai.ui.hr.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.remote.HrRequestDetailsDto
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.io.File

class HrRequestDetailsViewModel(
    private val repo: RequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _details = MutableLiveData<HrRequestDetailsDto?>(null)
    val details: LiveData<HrRequestDetailsDto?> = _details

    fun load(id: Int) {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadHrRequestDetails(id)) {
                is Result.Ok -> {
                    _details.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun send(id: Int, text: String?, file: File?) {
        val current = _details.value
        if (current?.status == "CLOSED") return
        if ((text.isNullOrBlank()) && (file == null)) return

        _sending.value = true
        viewModelScope.launch {
            when (val res = repo.sendHrMessage(id, text?.takeIf { it.isNotBlank() }, file)) {
                is Result.Ok -> {
                    _details.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _sending.value = false
        }
    }

    fun setInProgress(id: Int) {
        val current = _details.value
        if (current?.status == "CLOSED") return

        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.setInProgress(id)) {
                is Result.Ok -> _details.value = res.value
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun close(id: Int) {
        val current = _details.value
        if (current?.status == "CLOSED") return

        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.closeRequest(id)) {
                is Result.Ok -> _details.value = res.value
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }
}
