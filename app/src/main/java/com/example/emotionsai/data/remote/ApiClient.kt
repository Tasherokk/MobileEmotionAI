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
    private val baseUrl = "http://185.5.206.121/"

    private fun logging(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Только заголовки, без тела
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
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    val api: ApiService by lazy {
        retrofit(mainOkHttp).create(ApiService::class.java)
    }
}
