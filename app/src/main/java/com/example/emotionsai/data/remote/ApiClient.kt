package com.example.emotionsai.data.remote

import com.example.emotionsai.data.local.TokenStorage
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient(
    private val tokenStorage: TokenStorage
) {
    // IMPORTANT for emulator:
    private val baseUrl = "http://10.0.2.2:8000/"

    private fun logging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun retrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // client used ONLY for refresh to avoid recursion
    private val refreshOnlyOkHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging())
            .build()
    }

    val refreshApi: ApiService by lazy {
        retrofit(refreshOnlyOkHttp).create(ApiService::class.java)
    }

    private val mainOkHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(logging())
            .addInterceptor(AuthInterceptor(tokenStorage))
            .authenticator(TokenAuthenticator(tokenStorage, refreshApi))
            .build()
    }

    val api: ApiService by lazy {
        retrofit(mainOkHttp).create(ApiService::class.java)
    }
}
