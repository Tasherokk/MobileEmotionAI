package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.PhotoLoginResponse
import com.example.emotionsai.util.PhotoVerifyResult
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class FaceAuthRepository(
    private val api: ApiService
) {
    private val PART_NAME = "photo" // ✅ как ты просила
    private val gson = com.google.gson.Gson()
    suspend fun verifyFace(photoFile: File): PhotoVerifyResult {
        return try {
            val rb = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(PART_NAME, photoFile.name, rb)

            val ok = api.photoLogin(part) // 2xx
            mapVerdict(ok.verdict, ok.detail)

        } catch (e: HttpException) {
            val parsed = parseBodySafe(e.response()?.errorBody()?.string())

            // ✅ если даже при 401 пришёл verdict — это бизнес-результат, НЕ ошибка сессии
            if (parsed?.verdict != null) {
                mapVerdict(parsed.verdict, parsed.detail)
            } else {
                // ✅ 503/500/любое — это реальная ошибка сервера/сети
                PhotoVerifyResult.Error(e.code(), e)
            }

        } catch (e: Exception) {
            PhotoVerifyResult.Error(null, e)
        }
    }
    private fun parseBodySafe(raw: String?): PhotoLoginResponse? {
        return try {
            if (raw.isNullOrBlank()) null else gson.fromJson(raw, PhotoLoginResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }
    private fun mapVerdict(verdict: String?, detail: String?): PhotoVerifyResult {
        return when (verdict?.uppercase()) {
            "YES" -> PhotoVerifyResult.Approved
            "NO" -> PhotoVerifyResult.Rejected(detail?.takeIf { it.isNotBlank() } ?: "Лицо не совпало")
            else -> PhotoVerifyResult.Error(null, IllegalStateException("Unknown verdict: $verdict"))
        }
    }
    private fun parseErrorBody(e: HttpException): PhotoLoginResponse? {
        return try {
            val raw = e.response()?.errorBody()?.string() ?: return null
            // если у тебя есть Moshi/Gson — используй его.
            // Пример с Gson:
            com.google.gson.Gson().fromJson(raw, PhotoLoginResponse::class.java)
        } catch (_: Exception) {
            null
        }
    }
}



