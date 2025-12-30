package com.example.emotionsai.data.repo

import com.example.emotionsai.data.local.TokenStorage
import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.LoginRequest
import com.example.emotionsai.data.remote.RegisterRequest
import com.example.emotionsai.util.Result

class AuthRepository(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) {
    suspend fun login(username: String, password: String): Result<Unit> {
        return try {
            val tokens = api.login(LoginRequest(username.trim().lowercase(), password))
            tokenStorage.saveTokens(tokens.access, tokens.refresh)
            Result.Ok(Unit)
        } catch (e: Exception) {
            Result.Err(e.message ?: "Login failed")
        }
    }

    suspend fun register(username: String, name: String, password: String): Result<Unit> {
        return try {
            val tokens = api.register(RegisterRequest(username.trim().lowercase(), name, password))
            tokenStorage.saveTokens(tokens.access, tokens.refresh)
            Result.Ok(Unit)
        } catch (e: Exception) {
            Result.Err(e.message ?: "Register failed")
        }
    }

    fun logout() {
        tokenStorage.clear()
    }
}
