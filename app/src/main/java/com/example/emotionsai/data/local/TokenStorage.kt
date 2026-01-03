package com.example.emotionsai.data.local

import android.content.Context

class TokenStorage(context: Context) {
    private val sp = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    fun getAccess(): String? = sp.getString("access", null)
    fun getRefresh(): String? = sp.getString("refresh", null)
    fun getRole(): String? = sp.getString("role", null)
    fun saveTokens(access: String, refresh: String, role: String) {
        sp.edit()
            .putString("access", access)
            .putString("refresh", refresh)
            .putString("role", role)
            .apply()
    }

    fun updateAccess(access: String) {
        sp.edit()
            .putString("access", access)
            .apply()
    }

    fun clear() {
        sp.edit()
            .clear()
            .apply()
    }

    fun isLoggedIn(): Boolean = !getRefresh().isNullOrBlank()
}
