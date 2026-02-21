package com.example.emotionsai.data.remote

import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

/**
 * Incoming message from the WebSocket.
 * Fields match the backend's notify_new_message payload.
 */
data class WsMessageDto(
    val id: Int,
    val sender: Int,
    val sender_username: String,
    val sender_name: String,
    val text: String,
    val file: String?,
    val created_at: String
)

class ChatWebSocket(
    private val baseWsUrl: String = "ws://185.5.206.121/ws/chat/"
) {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()

    /**
     * Returns a cold Flow that connects on collection and closes on cancellation.
     * Each emitted value is a new incoming message.
     */
    fun connect(requestId: Int, token: String): Flow<WsMessageDto> = callbackFlow {
        val url = "${baseWsUrl}${requestId}/?token=$token"
        val request = Request.Builder().url(url).build()

        val ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                val msg = gson.fromJson(text, WsMessageDto::class.java)
                trySend(msg)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                close()
            }
        })

        awaitClose { ws.close(1000, "Fragment destroyed") }
    }
}
