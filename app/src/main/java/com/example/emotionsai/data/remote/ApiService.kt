package com.example.emotionsai.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

enum class UserRole { HR, EMPLOYEE;

    companion object {
        fun from(value: String) = runCatching { valueOf(value) }.getOrDefault(EMPLOYEE)
    }
}
data class RegisterRequest(val username: String, val name: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refresh: String)

//data class TokenPairResponse(val access: String, val refresh: String, val role: String)
data class UserDto(val id: Long, val username: String, val name: String, val role: String)

data class AuthResponse(val access: String, val refresh: String, val user: UserDto)

data class AccessResponse(val access: String)

data class MeResponse(val id: Long, val username: String, val name: String, val role: String)

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): AccessResponse

    @GET("api/me")
    suspend fun me(): MeResponse
}
