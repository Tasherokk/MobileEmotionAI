package com.example.emotionsai.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.emotionsai.data.local.SettingsStorage
import com.example.emotionsai.data.local.TokenStorage
import com.example.emotionsai.data.remote.ApiClient
import com.example.emotionsai.data.repo.AuthRepository
import com.example.emotionsai.data.repo.EventRepository
import com.example.emotionsai.data.repo.FaceAuthRepository
import com.example.emotionsai.data.repo.FeedbackRepository
import com.example.emotionsai.data.repo.ReferenceRepository
import com.example.emotionsai.data.repo.RequestRepository
import com.example.emotionsai.data.repo.UserRepository
import com.example.emotionsai.ui.employee.events.EmployeeEventsViewModel
import com.example.emotionsai.ui.employee.requests.CreateRequestViewModel
import com.example.emotionsai.ui.employee.requests.EmployeeRequestDetailsViewModel
import com.example.emotionsai.ui.employee.requests.EmployeeRequestsViewModel
import com.example.emotionsai.ui.hr.analytics.HrAnalyticsViewModel
import com.example.emotionsai.ui.hr.events.CreateEventViewModel
import com.example.emotionsai.ui.hr.events.HrEventsViewModel
import com.example.emotionsai.ui.hr.requests.HrRequestDetailsViewModel
import com.example.emotionsai.ui.hr.requests.HrRequestsViewModel

object ServiceLocator {
    @Volatile private var tokenStorage: TokenStorage? = null
    @Volatile private var apiClient: ApiClient? = null
    @Volatile private var authRepo: AuthRepository? = null
    @Volatile private var userRepo: UserRepository? = null
    @Volatile private var feedbackRepo: FeedbackRepository? = null
    @Volatile private var referenceRepo: ReferenceRepository? = null
    @Volatile private var faceAuthRepo: FaceAuthRepository? = null
    @Volatile private var settingsStorage: SettingsStorage? = null
    @Volatile private var eventRepository: EventRepository? = null
    @Volatile private var requestRepo: RequestRepository? = null
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

    // 3) Factory для HrAnalyticsViewModel
    // ServiceLocator.kt
    fun hrAnalyticsVMFactory(ctx: Context): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val api = apiClient(ctx).api // как ты создаёшь retrofit
                val repo = FeedbackRepository(api).also { feedbackRepo= it }
                @Suppress("UNCHECKED_CAST")
                return HrAnalyticsViewModel(repo) as T
            }
        }
    }


    fun referenceRepository(context: Context): ReferenceRepository {
        return referenceRepo ?: synchronized(this) {
            val api = apiClient(context).api
            ReferenceRepository(api).also { referenceRepo = it }
        }
    }
    fun settingsStorage(context: Context): SettingsStorage {
        return settingsStorage ?: synchronized(this) {
            settingsStorage ?: SettingsStorage(context.applicationContext).also { settingsStorage = it }
        }
    }

    fun faceAuthRepository(context: Context): FaceAuthRepository {
        return faceAuthRepo ?: synchronized(this) {
            val api = apiClient(context).api
            FaceAuthRepository(api).also { faceAuthRepo = it }
        }
    }
    fun eventRepository(context: Context): EventRepository {
        return eventRepository ?: synchronized(this) {
            val api = apiClient(context).api
            EventRepository(api).also { eventRepository = it }
        }
    }
    fun createEventVMFactory(ctx: Context) =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = eventRepository(ctx)
                return CreateEventViewModel(repo) as T
            }
        }
    fun employeeEventsVMFactory(context: Context): ViewModelProvider.Factory {
        val appContext = context.applicationContext
        return object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(EmployeeEventsViewModel::class.java)) {
                    val api = apiClient(appContext).api // твой метод получения ApiService
                    val repo = EventRepository(api)
                    return EmployeeEventsViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
    fun hrEventsVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val api = apiClient(ctx).api
            val repo = EventRepository(api)
            return HrEventsViewModel(repo) as T
        }
    }
    fun requestRepository(context: Context): RequestRepository {
        return requestRepo ?: synchronized(this) {
            val api = apiClient(context).api
            RequestRepository(api).also { requestRepo = it }
        }
    }
    fun employeeRequestsVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = requestRepository(ctx)
            return EmployeeRequestsViewModel(repo) as T
        }
    }

    fun createRequestVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = requestRepository(ctx)
            return CreateRequestViewModel(repo) as T
        }
    }

    fun employeeRequestDetailsVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = requestRepository(ctx)
            return EmployeeRequestDetailsViewModel(repo) as T
        }
    }

    fun hrRequestsVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = requestRepository(ctx)
            return HrRequestsViewModel(repo) as T
        }
    }

    fun hrRequestDetailsVMFactory(ctx: Context) = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val repo = requestRepository(ctx)
            return HrRequestDetailsViewModel(repo) as T
        }
    }

}
