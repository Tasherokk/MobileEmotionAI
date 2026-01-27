package com.example.emotionsai.data.remote

import com.example.emotionsai.data.local.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class TokenAuthenticator(
    private val tokenStorage: TokenStorage,
    private val refreshApi: ApiService // отдельный retrofit/okhttp без authenticator
) : Authenticator {

    private val lock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath

        if (path.endsWith("/api/auth/photo-login")) return null
        if (path.endsWith("/api/auth/refresh")) return null


        if (responseCount(response) >= 3) return null

        val refresh = tokenStorage.getRefresh() ?: return null

        lock.withLock {
            val currentAccess = tokenStorage.getAccess()
            val requestAccess = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            if (!currentAccess.isNullOrBlank() && currentAccess != requestAccess) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentAccess")
                    .build()
            }

            val newAccess = runBlocking {
                try {
                    refreshApi.refresh(RefreshRequest(refresh)).access
                } catch (_: Exception) {
                    null
                }
            } ?: return null

            tokenStorage.updateAccess(newAccess)

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccess")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var res: Response? = response
        var count = 1
        while (res?.priorResponse != null) {
            count++
            res = res.priorResponse
        }
        return count
    }
}
