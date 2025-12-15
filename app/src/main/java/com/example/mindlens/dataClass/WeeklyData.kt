package com.example.mindlens.dataClass

import androidx.compose.ui.graphics.Color

// data structure for storing weekly data to be displayed in home screen
data class WeeklyData(
    val day: String,
    val score: Float,
    val color: Color
)