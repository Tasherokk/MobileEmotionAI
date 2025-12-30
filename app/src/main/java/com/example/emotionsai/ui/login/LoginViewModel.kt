package com.example.emotionsai.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.di.ServiceLocator
import kotlinx.coroutines.launch
import com.example.emotionsai.util.Result

class LoginViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ServiceLocator.authRepository(app)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _success = MutableLiveData(false)
    val success: LiveData<Boolean> = _success

    fun login(username: String, password: String) {
        _error.value = ""
        _loading.value = true
        viewModelScope.launch {
            when (val r = repo.login(username, password)) {
                is Result.Ok -> _success.value = true
                is Result.Err -> _error.value = r.message
            }
            _loading.value = false
        }
    }

    fun register(username: String, name: String, password: String) {
        _error.value = ""
        _loading.value = true
        viewModelScope.launch {
            when (val r = repo.register(username, name, password)) {
                is Result.Ok -> _success.value = true
                is Result.Err -> _error.value = r.message
            }
            _loading.value = false
        }
    }
}
