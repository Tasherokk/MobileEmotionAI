package com.example.emotionsai.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
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

data class HrEventDto(
    val id: Int, 
    val title: String, 
    val starts_at: String, 
    val ends_at: String?, 
    val company: Int, 
    val company_name: String,
    val participants_count: Int
)

data class EmployeeEventDto(
    val id: Int,
    val title: String,
    val starts_at: String,
    val ends_at: String?,
    val company: Int,
    val company_name: String,
    val participants_count: Int,
    val has_feedback: Boolean
)

data class EventCreateRequest(
    val title: String,
    val starts_at: String,
    val ends_at: String?,
    val company: Int? = null,
    val participants: List<Int>? = null   // ← безопасно, nullable
)

data class EmployeeDto(
    val id: Int,
    val username: String,
    val name: String,
    val company: Int,
    val company_name: String?,
    val department: Int?,
    val department_name: String?
)


// ==================== Feedback Models ====================
data class FeedbackResponse(
    val id: Long,
    val emotion: String,
)
data class Feedback(
    val id: Long,
    val user_id: Long,
    val user_username: String,
    val emotion: String,
    val created_at: String,
    val department: Int?,
    val department_name: String?,
    val event: Int?
)

data class PhotoLoginResponse(
    val verdict: String? = null,   // "YES" | "NO"
    val detail: String? = null
) {
    val isApproved: Boolean
        get() = verdict.equals("YES", ignoreCase = true)
}
// ==================== Requests ====================
typealias RequestStatus = String // "OPEN" | "IN_PROGRESS" | "CLOSED"

data class RequestTypeDto(
    val id: Int,
    val name: String,
    val description: String
)

data class HrShortDto(
    val id: Int,
    val username: String,
    val name: String
)

// ---------- List items ----------

// Employee: GET /api/employee/requests/
data class EmployeeRequestItemDto(
    val id: Int,
    val type: Int,
    val type_name: String,
    val hr: Int,
    val hr_username: String,
    val hr_name: String,
    val status: RequestStatus,
    val created_at: String,
    val closed_at: String?,
    val messages_count: Int,
    val last_message_at: String?
)

// HR: GET /api/hr/requests/
data class HrRequestItemDto(
    val id: Int,
    val type: Int,
    val type_name: String,
    val employee: Int,
    val employee_username: String,
    val employee_name: String,
    val status: RequestStatus,
    val created_at: String,
    val closed_at: String?,
    val messages_count: Int,
    val last_message_at: String?
)

// ---------- Details ----------

data class RequestMessageDto(
    val id: Int,
    val sender: Int,
    val sender_username: String,
    val sender_name: String,
    val text: String?,
    val file: String?,
    val created_at: String,
    val is_mine: Boolean
)

// Employee details: GET /api/employee/requests/{id}/
data class EmployeeRequestDetailsDto(
    val id: Int,
    val type: Int,
    val type_name: String,
    val type_description: String?,
    val hr: Int,
    val hr_username: String,
    val hr_name: String,
    val status: RequestStatus,
    val created_at: String,
    val closed_at: String?,
    val messages: List<RequestMessageDto>
)

// HR details: GET /api/hr/requests/{id}/
data class HrRequestDetailsDto(
    val id: Int,
    val type: Int,
    val type_name: String,
    val type_description: String?,
    val employee: Int,
    val employee_username: String,
    val employee_name: String,
    val employee_department: String?,
    val status: RequestStatus,
    val created_at: String,
    val closed_at: String?,
    val messages: List<RequestMessageDto>
)

data class HrEventDetailsDto(
    val id: Int,
    val title: String,
    val starts_at: String,
    val ends_at: String?,
    val company: Int,
    val company_name: String,
    val participants: List<Int>,
    val participants_count: Int
)

// ---------- Bodies ----------

data class CreateEmployeeRequestBody(
    val type: Int,
    val hr: Int,
    val comment: String
)

data class UpdateRequestStatusBody(
    val status: String // "IN_PROGRESS"
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

    @GET("api/auth/me")
    suspend fun me(): MeResponse
    
    // ============ References ============
    @GET("api/feedback/companies")
    suspend fun getCompanies(): List<Company>
    
    @GET("api/feedback/departments")
    suspend fun getDepartments(@Query("company_id") companyId: Int? = null): List<Department>
    
    // ============ Employee Feedback ============
    @Multipart
    @POST("api/employee/feedback")
    suspend fun submitFeedback(
        @Part file: MultipartBody.Part,
        @Part("event_id") eventId: RequestBody? = null
    ): FeedbackResponse
    @GET("api/employee/events/my")
    suspend fun getMyEmployeeEvents(): List<EmployeeEventDto>
    // ============ HR Analytics ============
    @GET("api/hr/analytics/feedbacks/")
    suspend fun getHrFilteredFeedbacks(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("departments") departments: String? = null,
        @Query("emotions") emotions: String? = null,
        @Query("event_id") eventId: Int? = null,
        @Query("has_event") hasEvent: Boolean? = null
    ): List<Feedback>
    // ============ HR Event Management ===========
    @GET("api/hr/events/")
    suspend fun getHrEvents(): List<HrEventDto>

    @POST("api/hr/events/")
    suspend fun createHrEvent(@Body req: EventCreateRequest): HrEventDto

    @PUT("api/hr/events/{id}/")
    suspend fun updateHrEvent(
        @Path("id") id: Int,
        @Body req: EventCreateRequest
    ): HrEventDto

    @DELETE("api/hr/events/{id}/")
    suspend fun deleteHrEvent(@Path("id") id: Int)

    @GET("api/hr/company/employees")
    suspend fun getCompanyEmployees(): List<EmployeeDto>

    // ============ Face ID Auth ============
    @Multipart
    @POST("api/auth/photo-login")
    suspend fun photoLogin(
        @Part photo: MultipartBody.Part
    ): PhotoLoginResponse
    // ---------- Employee requests ----------
    @GET("api/employee/requests/")
    suspend fun getMyEmployeeRequests(): List<EmployeeRequestItemDto>

    @GET("api/employee/requests/types/")
    suspend fun getEmployeeRequestTypes(): List<RequestTypeDto>

    @GET("api/employee/requests/hr-list/")
    suspend fun getEmployeeHrList(): List<HrShortDto>

    @POST("api/employee/requests/")
    suspend fun createEmployeeRequest(@Body body: CreateEmployeeRequestBody): EmployeeRequestDetailsDto

    @GET("api/employee/requests/{id}/")
    suspend fun getEmployeeRequestDetails(@Path("id") id: Int): EmployeeRequestDetailsDto

    @Multipart
    @POST("api/employee/requests/{id}/messages/")
    suspend fun sendEmployeeRequestMessage(
        @Path("id") id: Int,
        @Part("text") text: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): EmployeeRequestDetailsDto


    // ---------- HR requests ----------
    @GET("api/hr/requests/")
    suspend fun getHrRequests(): List<HrRequestItemDto>

    @GET("api/hr/requests/{id}/")
    suspend fun getHrRequestDetails(@Path("id") id: Int): HrRequestDetailsDto

    @Multipart
    @POST("api/hr/requests/{id}/messages/")
    suspend fun sendHrRequestMessage(
        @Path("id") id: Int,
        @Part("text") text: RequestBody? = null,
        @Part file: MultipartBody.Part? = null
    ): HrRequestDetailsDto

    @POST("api/hr/requests/{id}/close/")
    suspend fun closeHrRequest(@Path("id") id: Int): HrRequestDetailsDto

    @PATCH("api/hr/requests/{id}/status/")
    suspend fun updateHrRequestStatus(
        @Path("id") id: Int,
        @Body body: UpdateRequestStatusBody
    ): HrRequestDetailsDto

    @GET("api/hr/events/{id}/")
    suspend fun getHrEventDetails(@Path("id") id: Int): HrEventDetailsDto

}
