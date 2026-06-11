package com.example.emotionsai

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.emotionsai.di.ServiceLocator

class EmotionsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val dark = ServiceLocator.settingsStorage(this).isDarkModeEnabled()
        AppCompatDelegate.setDefaultNightMode(
            if (dark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
    }
}
