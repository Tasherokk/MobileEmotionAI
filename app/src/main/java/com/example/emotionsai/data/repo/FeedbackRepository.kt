package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.FeedbackResponse
import com.example.emotionsai.data.remote.MyFeedbackResponse
import com.example.emotionsai.data.remote.MyStatsResponse
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
            
            val eventIdBody = eventId?.let { 
                it.toString().toRequestBody("text/plain".toMediaTypeOrNull()) 
            }
            
            val response = api.submitFeedback(body, eventIdBody)
            Result.Ok(response)
        } catch (e: Exception) {
            Result.Err("Не удалось проанализировать фото. Попробуйте еще раз")
        }
    }

    /**
     * Получить историю моих feedback'ов
     */
    suspend fun getMyFeedback(limit: Int = 50, offset: Int = 0): Result<MyFeedbackResponse> {
        return try {
            Result.Ok(api.getMyFeedback(limit, offset))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load feedback history")
        }
    }

    /**
     * Получить мою статистику
     */
    suspend fun getMyStats(from: String? = null, to: String? = null): Result<MyStatsResponse> {
        return try {
            Result.Ok(api.getMyStats(from, to))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load statistics")
        }
    }
}
