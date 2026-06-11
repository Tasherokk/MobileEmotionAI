package com.example.emotionsai.util

import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserMessage(): String = when (this) {
    is UnknownHostException,
    is ConnectException -> "No internet connection"
    is SocketTimeoutException -> "Server is not responding. Check your connection."
    is IOException -> "Network error. Check your connection."
    is HttpException -> when (code()) {
        401, 403 -> "Authorization required"
        404 -> "Not found"
        500, 502, 503 -> "Server error. Try again later."
        else -> "Server error (${code()})"
    }
    else -> "Something went wrong. Try again later."
}
