package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.util.PhotoVerifyResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File

class FaceAuthRepository(
    private val api: ApiService
) {
    private val PART_NAME = "photo"

    suspend fun verifyFace(photoFile: File): PhotoVerifyResult {
        return try {
            val rb = photoFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData(PART_NAME, photoFile.name, rb)

            val response = api.photoLogin(part)

            if (response.isApproved) PhotoVerifyResult.Approved
            else PhotoVerifyResult.Rejected(response.detail.ifBlank { "Face verification failed" })

        } catch (e: HttpException) {
            PhotoVerifyResult.Error(e)
        } catch (e: Exception) {
            PhotoVerifyResult.Error(e)
        }
    }
}
