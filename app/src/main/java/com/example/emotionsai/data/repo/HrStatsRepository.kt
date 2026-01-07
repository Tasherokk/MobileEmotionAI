package com.example.emotionsai.data.repo

import com.example.emotionsai.data.remote.ApiService
import com.example.emotionsai.data.remote.Event
import com.example.emotionsai.data.remote.EventCreateRequest
import com.example.emotionsai.data.remote.HrByUserResponse
import com.example.emotionsai.data.remote.HrOverviewResponse
import com.example.emotionsai.data.remote.HrTimelineResponse
import com.example.emotionsai.util.Result

class HrStatsRepository(
    private val api: ApiService
) {
    /**
     * Получить общую статистику (overview)
     */
    suspend fun getOverview(
        from: String? = null,
        to: String? = null,
        eventId: Int? = null,
        department: String? = null
    ): Result<HrOverviewResponse> {
        return try {
            Result.Ok(api.getHrOverview(from, to, eventId, department))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load overview")
        }
    }

    /**
     * Получить график по времени (timeline)
     */
    suspend fun getTimeline(
        from: String? = null,
        to: String? = null,
        groupBy: String = "day",
        eventId: Int? = null,
        department: String? = null
    ): Result<HrTimelineResponse> {
        return try {
            Result.Ok(api.getHrTimeline(from, to, groupBy, eventId, department))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load timeline")
        }
    }

    /**
     * Получить статистику по пользователям
     */
    suspend fun getByUser(
        from: String? = null,
        to: String? = null,
        limit: Int = 20,
        eventId: Int? = null,
        department: String? = null
    ): Result<HrByUserResponse> {
        return try {
            Result.Ok(api.getHrByUser(from, to, limit, eventId, department))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load user statistics")
        }
    }

    /**
     * Создать событие
     */
    suspend fun createEvent(
        title: String,
        startsAt: String,
        endsAt: String?,
        companyId: Int
    ): Result<Event> {
        return try {
            val request = EventCreateRequest(title, startsAt, endsAt, companyId)
            Result.Ok(api.createEvent(request))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to create event")
        }
    }

    /**
     * Получить детали события
     */
    suspend fun getEvent(eventId: Int): Result<Event> {
        return try {
            Result.Ok(api.getEvent(eventId))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to load event")
        }
    }

    /**
     * Обновить событие
     */
    suspend fun updateEvent(
        eventId: Int,
        title: String,
        startsAt: String,
        endsAt: String?,
        companyId: Int
    ): Result<Event> {
        return try {
            val request = EventCreateRequest(title, startsAt, endsAt, companyId)
            Result.Ok(api.updateEvent(eventId, request))
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to update event")
        }
    }

    /**
     * Удалить событие
     */
    suspend fun deleteEvent(eventId: Int): Result<Unit> {
        return try {
            api.deleteEvent(eventId)
            Result.Ok(Unit)
        } catch (e: Exception) {
            Result.Err(e.message ?: "Failed to delete event")
        }
    }
}
