package com.example.emotionsai.ui.hr.analytics.theme

import androidx.compose.ui.graphics.Color

object EmotionColors {
    fun color(emotion: String): Color = when (emotion.lowercase()) {
        "happy" -> Color(0xFF2E7D32)      // спокойный зелёный
        "neutral" -> Color(0xFF607D8B)    // серо-синий
        "sad" -> Color(0xFF1565C0)        // синий
        "angry" -> Color(0xFFC62828)      // красный
        "disgust" -> Color(0xFF6A1B9A)    // фиолетовый
        "fear" -> Color(0xFF5D4037)       // коричневый
        "surprised" -> Color(0xFFEF6C00)  // оранжевый
        else -> Color(0xFF455A64)
    }
}