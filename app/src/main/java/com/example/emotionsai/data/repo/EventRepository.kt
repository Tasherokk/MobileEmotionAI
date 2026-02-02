package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.EmployeeDto
import com.example.emotionsai.data.remote.EmployeeEventDto
import com.example.emotionsai.data.remote.EventCreateRequest
import com.example.emotionsai.data.remote.HrEventDto
import com.example.emotionsai.util.Result


class EventRepository(
    private val api: ApiService
) {
    suspend fun loadMyEvents(): Result<List<EmployeeEventDto>> = try {
        Result.Ok(api.getMyEmployeeEvents())
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to load employee events")
    }

    // ===== HR: employees =====
    suspend fun getCompanyEmployees(): Result<List<EmployeeDto>> = try {
        Result.Ok(api.getCompanyEmployees())
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to load employees")
    }

    // ===== HR: events =====
    suspend fun loadHrEvents(): Result<List<HrEventDto>> = try {
        Result.Ok(api.getHrEvents())
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to load HR events")
    }

    suspend fun createHrEvent(req: EventCreateRequest): Result<HrEventDto> = try {
        Result.Ok(api.createHrEvent(req))
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to create event")
    }

    suspend fun updateHrEvent(id: Int, req: EventCreateRequest): Result<HrEventDto> = try {
        Result.Ok(api.updateHrEvent(id, req))
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to update event")
    }

    suspend fun deleteHrEvent(id: Int): Result<Unit> = try {
        api.deleteHrEvent(id)
        Result.Ok(Unit)
    } catch (e: Exception) {
        Result.Err(e.message ?: "Failed to delete event")
    }
}

