package com.example.emotionsai.ui.employee.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.local.TokenStorage
import com.example.emotionsai.data.remote.ChatWebSocket
import com.example.emotionsai.data.remote.EmployeeRequestDetailsDto
import com.example.emotionsai.data.remote.RequestMessageDto
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

class EmployeeRequestDetailsViewModel(
    private val repo: RequestRepository,
    private val tokenStorage: TokenStorage,
    private val chatWs: ChatWebSocket = ChatWebSocket()
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _sending = MutableLiveData(false)
    val sending: LiveData<Boolean> = _sending

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _details = MutableLiveData<EmployeeRequestDetailsDto?>(null)
    val details: LiveData<EmployeeRequestDetailsDto?> = _details

    private var wsJob: Job? = null

    fun load(id: Int) {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadEmployeeRequestDetails(id)) {
                is Result.Ok -> {
                    _details.value = res.value
                    _error.value = null
                    connectWs(id)
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    private fun connectWs(requestId: Int) {
        wsJob?.cancel()
        val token = tokenStorage.getAccess() ?: return
        wsJob = viewModelScope.launch {
            chatWs.connect(requestId, token)
                .catch { /* WS error — silent, REST still works */ }
                .collect { wsMsg ->
                    val current = _details.value ?: return@collect
                    // Don't duplicate if we already have this message (e.g. we sent it)
                    if (current.messages.any { it.id == wsMsg.id }) return@collect

                    val newMsg = RequestMessageDto(
                        id = wsMsg.id,
                        sender = wsMsg.sender,
                        sender_username = wsMsg.sender_username,
                        sender_name = wsMsg.sender_name,
                        text = wsMsg.text,
                        file = wsMsg.file,
                        created_at = wsMsg.created_at,
                        is_mine = false
                    )
                    _details.value = current.copy(
                        messages = current.messages + newMsg
                    )
                }
        }
    }

    fun send(id: Int, text: String?, file: File?) {
        val current = _details.value
        if (current?.status == "CLOSED") return

        if ((text.isNullOrBlank()) && (file == null)) return

        _sending.value = true
        viewModelScope.launch {
            when (val res = repo.sendEmployeeMessage(id, text?.takeIf { it.isNotBlank() }, file)) {
                is Result.Ok -> {
                    _details.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _sending.value = false
        }
    }

    override fun onCleared() {
        wsJob?.cancel()
        super.onCleared()
    }
}
