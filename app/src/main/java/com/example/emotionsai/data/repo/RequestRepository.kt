package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.*
import com.example.emotionsai.util.Result
import com.example.emotionsai.util.toUserMessage
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RequestRepository(
    private val api: ApiService
) {
    // ---- Employee ----
    suspend fun loadMyEmployeeRequests(): Result<List<EmployeeRequestItemDto>> = try {
        Result.Ok(api.getMyEmployeeRequests())
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun loadRequestTypes(): Result<List<RequestTypeDto>> = try {
        Result.Ok(api.getEmployeeRequestTypes())
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun loadHrList(): Result<List<HrShortDto>> = try {
        Result.Ok(api.getEmployeeHrList())
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun createRequest(typeId: Int, hrId: Int, comment: String): Result<EmployeeRequestDetailsDto> = try {
        Result.Ok(api.createEmployeeRequest(CreateEmployeeRequestBody(type = typeId, hr = hrId, comment = comment)))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun loadEmployeeRequestDetails(id: Int): Result<EmployeeRequestDetailsDto> = try {
        Result.Ok(api.getEmployeeRequestDetails(id))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun sendEmployeeMessage(id: Int, text: String?, file: File?): Result<EmployeeRequestDetailsDto> = try {
        Result.Ok(api.sendEmployeeRequestMessage(id, text?.toTextPart(), file?.toFilePart("file")))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    // ---- HR ----
    suspend fun loadHrRequests(): Result<List<HrRequestItemDto>> = try {
        Result.Ok(api.getHrRequests())
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun loadHrRequestDetails(id: Int): Result<HrRequestDetailsDto> = try {
        Result.Ok(api.getHrRequestDetails(id))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun sendHrMessage(id: Int, text: String?, file: File?): Result<HrRequestDetailsDto> = try {
        Result.Ok(api.sendHrRequestMessage(id, text?.toTextPart(), file?.toFilePart("file")))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun closeRequest(id: Int): Result<HrRequestDetailsDto> = try {
        Result.Ok(api.closeHrRequest(id))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }

    suspend fun setInProgress(id: Int): Result<HrRequestDetailsDto> = try {
        Result.Ok(api.updateHrRequestStatus(id, UpdateRequestStatusBody(status = "IN_PROGRESS")))
    } catch (e: Exception) {
        Result.Err(e.toUserMessage())
    }
}

// ---------- helpers ----------
private fun String.toTextPart(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())

private fun File.toFilePart(partName: String): MultipartBody.Part {
    val rb = this.asRequestBody("application/octet-stream".toMediaType())
    return MultipartBody.Part.createFormData(partName, name, rb)
}
