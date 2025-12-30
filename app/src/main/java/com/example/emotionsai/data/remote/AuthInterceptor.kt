package com.example.emotionsai.data.remote

import com.example.emotionsai.data.local.TokenStorage
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val access = tokenStorage.getAccess()

        val req = if (!access.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $access")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(req)
    }
}
