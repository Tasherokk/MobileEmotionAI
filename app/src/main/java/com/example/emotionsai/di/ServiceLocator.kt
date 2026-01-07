package com.example.emotionsai.di

import android.content.Context
import com.example.emotionsai.data.local.TokenStorage
import com.example.emotionsai.data.remote.ApiClient
import com.example.emotionsai.data.repo.AuthRepository
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.data.repo.HrStatsRepository
import com.example.emotionsai.data.repo.ReferenceRepository
import com.example.emotionsai.data.repo.UserRepository

object ServiceLocator {
    @Volatile private var tokenStorage: TokenStorage? = null
    @Volatile private var apiClient: ApiClient? = null
    @Volatile private var authRepo: AuthRepository? = null
    @Volatile private var userRepo: UserRepository? = null
    @Volatile private var feedbackRepo: FeedbackRepository? = null
    @Volatile private var hrStatsRepo: HrStatsRepository? = null
    @Volatile private var referenceRepo: ReferenceRepository? = null

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

    fun feedbackRepository(context: Context): FeedbackRepository {
        return feedbackRepo ?: synchronized(this) {
            val api = apiClient(context).api
            FeedbackRepository(api).also { feedbackRepo = it }
        }
    }

    fun hrStatsRepository(context: Context): HrStatsRepository {
        return hrStatsRepo ?: synchronized(this) {
            val api = apiClient(context).api
            HrStatsRepository(api).also { hrStatsRepo = it }
        }
    }

    fun referenceRepository(context: Context): ReferenceRepository {
        return referenceRepo ?: synchronized(this) {
            val api = apiClient(context).api
            ReferenceRepository(api).also { referenceRepo = it }
        }
    }
}
