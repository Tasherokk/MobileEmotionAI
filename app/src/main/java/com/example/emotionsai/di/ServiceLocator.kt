package com.example.emotionsai.di

import android.content.Context
import com.example.emotionsai.data.local.TokenStorage
import com.example.emotionsai.data.remote.ApiClient
import com.example.emotionsai.data.repo.AuthRepository
import com.example.emotionsai.data.repo.UserRepository

object ServiceLocator {
    @Volatile private var tokenStorage: TokenStorage? = null
    @Volatile private var apiClient: ApiClient? = null
    @Volatile private var authRepo: AuthRepository? = null
    @Volatile private var userRepo: UserRepository? = null

    fun tokenStorage(context: Context): TokenStorage {
        return tokenStorage ?: synchronized(this) {
            tokenStorage ?: TokenStorage(context.applicationContext).also { tokenStorage = it }
        }
    }

    fun apiClient(context: Context): ApiClient {
        return apiClient ?: synchronized(this) {
            apiClient ?: ApiClient(tokenStorage(context)).also { apiClient = it }
        }
    }

    fun authRepository(context: Context): AuthRepository {
        return authRepo ?: synchronized(this) {
            val api = apiClient(context).api
            AuthRepository(api, tokenStorage(context)).also { authRepo = it }
        }
    }

    fun userRepository(context: Context): UserRepository {
        return userRepo ?: synchronized(this) {
            val api = apiClient(context).api
            UserRepository(api).also { userRepo = it }
        }
    }
}
