package com.example.emotionsai.ui.hr.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.emotionsai.data.remote.MeResponse
import com.example.emotionsai.di.ServiceLocator
import kotlinx.coroutines.launch
import com.example.emotionsai.util.Result

class HrProfileViewModel(app: Application) : AndroidViewModel(app) {
    private val userRepo = ServiceLocator.userRepository(app)
    private val authRepo = ServiceLocator.authRepository(app)

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _me = MutableLiveData<MeResponse?>(null)
    val me: LiveData<MeResponse?> = _me

    private val _error = MutableLiveData("")
    val error: LiveData<String> = _error

    private val _forceLogout = MutableLiveData<Boolean?>(null)
    val forceLogout: LiveData<Boolean?> = _forceLogout

    fun loadMe() {
        _error.value = ""
        _loading.value = true
        viewModelScope.launch {
            when (val r = userRepo.me()) {
                is Result.Ok -> _me.value = r.value
                is Result.Err -> {
                    _error.value = r.message
                }
            }
            _loading.value = false
        }
    }

    fun logout() {
        authRepo.logout()
        _forceLogout.value = true
    }

    fun logoutHandled() {
        _forceLogout.value = null
    }
}
