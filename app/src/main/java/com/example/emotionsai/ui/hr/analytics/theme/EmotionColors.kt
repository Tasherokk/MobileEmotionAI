package com.example.emotionsai.ui.hr.analytics.theme

import androidx.compose.ui.graphics.Color

object EmotionColors {
    fun color(emotion: String): Color = when (emotion.lowercase()) {
        "happy" -> Color(0xFF10B981)      // Emerald
        "neutral" -> Color(0xFF6366F1)    // Indigo
        "sad" -> Color(0xFF3B82F6)        // Blue
        "angry" -> Color(0xFFEF4444)      // Red
        "disgust" -> Color(0xFF8B5CF6)    // Violet
        "fear" -> Color(0xFF1E293B)       // Slate
        "surprise" -> Color(0xFFF59E0B)   // Amber
        else -> Color(0xFF94A3B8)
    }
}
