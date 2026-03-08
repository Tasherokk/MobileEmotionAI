package com.example.emotionsai.ui.employee.requests

import androidx.lifecycle.*
import com.example.emotionsai.data.remote.EmployeeRequestItemDto
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.util.Result
import kotlinx.coroutines.launch

class EmployeeRequestsViewModel(
    private val repo: RequestRepository
) : ViewModel() {

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _items = MutableLiveData<List<EmployeeRequestItemDto>>(emptyList())
    val items: LiveData<List<EmployeeRequestItemDto>> = _items

    private var isDescending = true

    fun load() {
        _loading.value = true
        viewModelScope.launch {
            when (val res = repo.loadMyEmployeeRequests()) {
                is Result.Ok -> {
                    _items.value = sortItems(res.value)
                    _error.value = null
                }
                is Result.Err -> _error.value = res.message
            }
            _loading.value = false
        }
    }

    fun toggleSortOrder() {
        isDescending = !isDescending
        _items.value = sortItems(_items.value ?: emptyList())
    }

    private fun sortItems(list: List<EmployeeRequestItemDto>): List<EmployeeRequestItemDto> {
        return if (isDescending) {
            list.sortedByDescending { it.created_at }
        } else {
            list.sortedBy { it.created_at }
        }
    }
}
