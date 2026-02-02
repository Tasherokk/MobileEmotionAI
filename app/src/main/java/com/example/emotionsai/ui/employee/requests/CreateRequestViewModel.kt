package com.example.emotionsai.ui.employee.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.remote.*
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CreateRequestViewModel(
    private val repo: RequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _types = MutableLiveData<List<RequestTypeDto>>(emptyList())
    val types: LiveData<List<RequestTypeDto>> = _types

    private val _hrs = MutableLiveData<List<HrShortDto>>(emptyList())
    val hrs: LiveData<List<HrShortDto>> = _hrs

    private val _created = MutableLiveData<EmployeeRequestDetailsDto?>(null)
    val created: LiveData<EmployeeRequestDetailsDto?> = _created

    fun loadRefs() {
        _loading.value = true
        viewModelScope.launch {
            val typesDef = async { repo.loadRequestTypes() }
            val hrsDef = async { repo.loadHrList() }

            when (val r1 = typesDef.await()) {
                is Result.Ok -> _types.value = r1.value
                is Result.Err -> _error.value = r1.message
            }
            when (val r2 = hrsDef.await()) {
                is Result.Ok -> _hrs.value = r2.value
                is Result.Err -> _error.value = r2.message
            }
            _loading.value = false
        }
    }

    fun create(typeId: Int, hrId: Int, comment: String) {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.createRequest(typeId, hrId, comment)) {
                is Result.Ok -> {
                    _created.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun createdHandled() {
        _created.value = null
    }
}
