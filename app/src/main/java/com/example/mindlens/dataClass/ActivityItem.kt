package com.example.mindlens.dataClass

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Data structure for storing various activities in Mind & Body section
data class ActivityItem(
    val title: String,
    val duration: String,
    val icon: ImageVector,
    val color: Color
)