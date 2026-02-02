package com.example.emotionsai.ui.employee.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.remote.EmployeeRequestDetailsDto
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch
import java.io.File

class EmployeeRequestDetailsViewModel(
    private val repo: RequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _details = MutableLiveData<EmployeeRequestDetailsDto?>(null)
    val details: LiveData<EmployeeRequestDetailsDto?> = _details

    fun load(id: Int) {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadEmployeeRequestDetails(id)) {
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
        // защита от отправки в CLOSED — по твоему правилу
        val current = _details.value
        if (current?.status == "CLOSED") return

        if ((text.isNullOrBlank()) && (file == null)) return

        _sending.value = true
        viewModelScope.launch {
            when (val res = repo.sendEmployeeMessage(id, text?.takeIf { it.isNotBlank() }, file)) {
                is Result.Ok -> {
                    _details.value = res.value // обновился список сообщений
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _sending.value = false
        }
    }
}
