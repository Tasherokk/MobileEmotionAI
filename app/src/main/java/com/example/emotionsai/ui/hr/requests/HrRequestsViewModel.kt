package com.example.emotionsai.ui.hr.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.remote.HrRequestItemDto
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch

class HrRequestsViewModel(
    private val repo: RequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _items = MutableLiveData<List<HrRequestItemDto>>(emptyList())
    val items: LiveData<List<HrRequestItemDto>> = _items

    fun load() {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadHrRequests()) {
                is Result.Ok -> {
                    _items.value = res.value
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }
}
