package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.MeResponse
import com.example.emotionsai.util.Result

class UserRepository(
    private val api: ApiService
) {
    suspend fun me(): Result<MeResponse> {
        return try {
            Result.Ok(api.me())
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load profile")
        }
    }
}
