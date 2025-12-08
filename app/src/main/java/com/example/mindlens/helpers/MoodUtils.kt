package com.example.mindlens.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mindlens.ui.* // Sesuaikan import warna theme kamu

// Mapping Mood String ke Data Visual & Analitik
enum class MoodType(val label: String, val score: Float, val color: Color, val icon: ImageVector) {
    AWFUL("Awful", 0.2f, MoodTerrible, Icons.Outlined.SentimentVeryDissatisfied),
    BAD("Bad", 0.4f, MoodBad, Icons.Outlined.SentimentDissatisfied),
    OKAY("Okay", 0.6f, MoodNeutral, Icons.Outlined.SentimentNeutral),
    GOOD("Good", 0.8f, MoodGood, Icons.Outlined.SentimentSatisfied),
    GREAT("Great", 1.0f, MoodGreat, Icons.Outlined.SentimentVerySatisfied),
    NEUTRAL("Neutral", 0.5f, Color.Gray, Icons.Outlined.SentimentNeutral); // Fallback

    companion object {
        fun fromLabel(label: String): MoodType {
            return entries.find { it.label.equals(label, ignoreCase = true) } ?: NEUTRAL
        }
    }
}