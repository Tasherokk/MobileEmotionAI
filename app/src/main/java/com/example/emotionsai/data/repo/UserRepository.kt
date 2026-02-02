package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.MeResponse
import com.example.emotionsai.util.Result
import retrofit2.HttpException

class UserRepository(
    private val api: ApiService
) {
    suspend fun me(): Result<MeResponse> {
        return try {
            Result.Ok(api.me())
        } catch (e: HttpException) {
            Result.Err("HTTP_${e.code()}")
        }
    }
}
