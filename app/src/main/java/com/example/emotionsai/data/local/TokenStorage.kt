package com.example.emotionsai.data.local

import android.content.Context
import android.util.Log

class TokenStorage(context: Context) {
    private val sp = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    fun getAccess(): String? {
        val token = sp.getString("access", null)
        Log.d("TokenStorage", "getAccess: ${token?.take(20)}...")
        return token
    }
    
    fun getRefresh(): String? = sp.getString("refresh", null)
    fun getRole(): String? = sp.getString("role", null)
    
    fun saveTokens(access: String, refresh: String, role: String) {
        Log.d("TokenStorage", "saveTokens: access=${access.take(20)}..., role=$role")
        sp.edit()
            .putString("access", access)
            .putString("refresh", refresh)
            .putString("role", role)
            .apply()
    }

    fun updateAccess(access: String) {
        Log.d("TokenStorage", "updateAccess: ${access.take(20)}...")
        sp.edit()
            .putString("access", access)
            .apply()
    }

    fun clear() {
        Log.d("TokenStorage", "clear: tokens cleared")
        sp.edit()
            .clear()
            .apply()
    }

    fun isLoggedIn(): Boolean = !getRefresh().isNullOrBlank()
}
