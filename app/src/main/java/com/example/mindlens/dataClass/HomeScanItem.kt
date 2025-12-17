package com.example.mindlens.dataClass

// Data structure for storing recent scans with ML
data class HomeScanItem(
    val result: String,
    val date: String,
    val confidence: Int,
    val isRisk: Boolean
)