package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.Feedback
import com.example.emotionsai.data.remote.FeedbackResponse
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.util.Result
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class FeedbackRepository(
    private val api: ApiService
) {
    /**
     * Отправить фото для анализа эмоций
     */

    suspend fun submitFeedback(photoFile: File, eventId: Int? = null): Result<FeedbackResponse> {
        return try {
            val requestFile = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", photoFile.name, requestFile)
            
            val eventIdBody = eventId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            val response = api.submitFeedback(body)
            Result.Ok(response)
        } catch (e: Exception) {
            Result.Err("Unable to analyze emotion: ${e.message}")
        }
    }
    suspend fun loadFeedbacks(
        start: String,
        end: String,
        departments: List<Int>? = null,
        emotions: List<String>? = null,
        eventId: Int? = null,
        hasEvent: Boolean? = null
    ): Result<List<Feedback>> {
        return try {

            val depStr = departments?.joinToString(",")
            val emoStr = emotions?.joinToString(",")

            val data = api.getHrFilteredFeedbacks(
                startDate = start,
                endDate = end,
                departments = depStr,
                emotions = emoStr,
                eventId = eventId,
                hasEvent = hasEvent
            )

            Result.Ok(data)

        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load analytics feedbacks")
        }
    }
    suspend fun loadHrEvents(): Result<List<HrEventDto>> {
        return try {
            Result.Ok(api.getHrEvents())
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load HR events")
        }
    }

}
