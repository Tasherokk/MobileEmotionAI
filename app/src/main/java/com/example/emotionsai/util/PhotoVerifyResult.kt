package com.example.emotionsai.util

sealed class PhotoVerifyResult {
    object Approved : PhotoVerifyResult()
    data class Rejected(val reason: String) : PhotoVerifyResult()
    data class Error(val throwable: Throwable) : PhotoVerifyResult()
}
