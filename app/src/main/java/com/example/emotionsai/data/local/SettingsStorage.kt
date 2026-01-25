package com.example.emotionsai.data.local

import android.content.Context
import androidx.core.content.edit

class SettingsStorage(context: Context) {
    private val sp = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun isFaceIdEnabled(): Boolean = sp.getBoolean("face_id_enabled", false)

    fun setFaceIdEnabled(enabled: Boolean) {
        sp.edit { putBoolean("face_id_enabled", enabled) }
    }
}
