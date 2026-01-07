package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.Company
import com.example.emotionsai.data.remote.Department
import com.example.emotionsai.data.remote.Event
import com.example.emotionsai.util.Result

class ReferenceRepository(
    private val api: ApiService
) {
    suspend fun getCompanies(): Result<List<Company>> {
        return try {
            Result.Ok(api.getCompanies())
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load companies")
        }
    }

    suspend fun getDepartments(companyId: Int? = null): Result<List<Department>> {
        return try {
            Result.Ok(api.getDepartments(companyId))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load departments")
        }
    }

    suspend fun getEvents(active: Boolean? = null, companyId: Int? = null): Result<List<Event>> {
        return try {
            Result.Ok(api.getEvents(active, companyId))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load events")
        }
    }
}
