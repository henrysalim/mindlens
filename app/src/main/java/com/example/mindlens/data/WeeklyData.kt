package com.example.mindlens.data

import androidx.compose.ui.graphics.Color

// for storing weekly data to be displayed in home screen
data class WeeklyData(
    val day: String,
    val score: Float,
    val color: Color
)