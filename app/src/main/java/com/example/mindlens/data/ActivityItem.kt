package com.example.mindlens.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class ActivityItem(
    val title: String,
    val duration: String,
    val icon: ImageVector,
    val color: Color
)