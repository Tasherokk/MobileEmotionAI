package com.example.emotionsai.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RegisterRequest(val username: String, val name: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refresh: String)

data class TokenPairResponse(val access: String, val refresh: String)
data class AccessResponse(val access: String)

data class MeResponse(val id: Long, val username: String, val name: String)

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): TokenPairResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): TokenPairResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): AccessResponse

    @GET("api/me")
    suspend fun me(): MeResponse
}
