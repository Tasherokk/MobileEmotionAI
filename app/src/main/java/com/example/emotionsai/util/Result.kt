package com.example.emotionsai.util

sealed class Result<out T> {
    data class Ok<T>(val value: T): Result<T>()
    data class Err(val message: String): Result<Nothing>()
}
