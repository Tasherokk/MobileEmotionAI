package com.example.emotionsai.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

enum class UserRole { HR, EMPLOYEE;

    companion object {
        fun from(value: String) = runCatching { valueOf(value) }.getOrDefault(EMPLOYEE)
    }
}

// ==================== Auth Models ====================
data class RegisterRequest(
    val username: String, 
    val name: String, 
    val password: String,
    val company_id: Int? = null,
    val department_id: Int? = null
)

data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refresh: String)

data class UserDto(
    val id: Long, 
    val username: String, 
    val name: String, 
    val role: String,
    val company: Int?,
    val company_name: String?,
    val department: Int?,
    val department_name: String?
)

data class AuthResponse(val access: String, val refresh: String, val user: UserDto)
data class AccessResponse(val access: String)
data class MeResponse(
    val id: Long, 
    val username: String, 
    val name: String, 
    val role: String,
    val company: Int?,
    val company_name: String?,
    val department: Int?,
    val department_name: String?
)

// ==================== Reference Models ====================
data class Company(val id: Int, val name: String)

data class Department(
    val id: Int, 
    val name: String, 
    val company: Int, 
    val company_name: String
)

data class Event(
    val id: Int, 
    val title: String, 
    val starts_at: String, 
    val ends_at: String?, 
    val company: Int, 
    val company_name: String
)

data class EventCreateRequest(
    val title: String,
    val starts_at: String,
    val ends_at: String?,
    val company: Int
)

// ==================== Feedback Models ====================
data class EmotionProb(val emotion: String, val prob: Float)

data class FaceBox(val x: Int, val y: Int, val w: Int, val h: Int)

data class FeedbackResponse(
    val id: Long,
    val emotion: String,
    val confidence: Float,
    val top3: List<EmotionProb>,
    val face_box: FaceBox?,
    val probs: Map<String, Float>
)

data class Feedback(
    val id: Long,
    val created_at: String,
    val emotion: String,
    val confidence: Float,
    val top3: List<EmotionProb>?,
    val face_box: FaceBox?,
    val probs: Map<String, Float>,
    val event: Int?,
    val event_title: String?,
    val company: Int?,
    val company_name: String?,
    val department: Int?,
    val department_name: String?
)

data class MyFeedbackResponse(
    val results: List<Feedback>,
    val total: Int,
    val limit: Int,
    val offset: Int
)

data class EmotionCount(val emotion: String, val count: Int, val percent: Float)

data class MyStatsResponse(
    val total: Int,
    val avg_confidence: Float,
    val top_emotion: String?,
    val emotions: List<EmotionCount>,
    val filters: Map<String, String?>
)

// ==================== HR Analytics Models ====================
data class HrOverviewResponse(
    val total: Int,
    val avg_confidence: Float,
    val top_emotion: String?,
    val emotions: List<EmotionCount>,
    val filters: Map<String, String?>
)

data class TimelinePoint(
    val bucket: String,
    val emotions: Map<String, Int>
)

data class HrTimelineResponse(
    val group_by: String,
    val series: List<TimelinePoint>,
    val filters: Map<String, String?>
)

data class UserStats(
    val user_id: Long,
    val name: String,
    val username: String,
    val total: Int,
    val avg_confidence: Float,
    val top_emotion: String?
)

data class HrByUserResponse(
    val users: List<UserStats>,
    val filters: Map<String, String?>
)

// ==================== API Interface ====================
interface ApiService {
    // ============ Auth ============
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): AccessResponse

    @GET("api/me")
    suspend fun me(): MeResponse
    
    // ============ References ============
    @GET("api/feedback/companies")
    suspend fun getCompanies(): List<Company>
    
    @GET("api/feedback/departments")
    suspend fun getDepartments(@Query("company_id") companyId: Int? = null): List<Department>
    
    @GET("api/feedback/events")
    suspend fun getEvents(
        @Query("active") active: Boolean? = null,
        @Query("company_id") companyId: Int? = null
    ): List<Event>
    
    // ============ Employee Feedback ============
    @Multipart
    @POST("api/feedback/photo")
    suspend fun submitFeedback(
        @Part file: MultipartBody.Part,
        @Part("event_id") eventId: RequestBody? = null
    ): FeedbackResponse
    
    @GET("api/feedback/my")
    suspend fun getMyFeedback(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): MyFeedbackResponse
    
    @GET("api/feedback/my/stats")
    suspend fun getMyStats(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): MyStatsResponse
    
    // ============ HR Analytics ============
    @GET("api/feedback/hr/stats/overview")
    suspend fun getHrOverview(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("event_id") eventId: Int? = null,
        @Query("department") department: String? = null
    ): HrOverviewResponse
    
    @GET("api/feedback/hr/stats/timeline")
    suspend fun getHrTimeline(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("group_by") groupBy: String = "day",
        @Query("event_id") eventId: Int? = null,
        @Query("department") department: String? = null
    ): HrTimelineResponse
    
    @GET("api/feedback/hr/stats/by_user")
    suspend fun getHrByUser(
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("event_id") eventId: Int? = null,
        @Query("department") department: String? = null
    ): HrByUserResponse
    
    // ============ HR Event Management ============
    @POST("api/feedback/hr/events")
    suspend fun createEvent(@Body req: EventCreateRequest): Event
    
    @GET("api/feedback/hr/events/{id}")
    suspend fun getEvent(@Path("id") id: Int): Event
    
    @PUT("api/feedback/hr/events/{id}")
    suspend fun updateEvent(@Path("id") id: Int, @Body req: EventCreateRequest): Event
    
    @DELETE("api/feedback/hr/events/{id}")
    suspend fun deleteEvent(@Path("id") id: Int)
}
