package com.example.mindlens.data

// For storing recent scans with ML
data class HomeScanItem(
    val result: String,
    val date: String,
    val confidence: Int,
    val isRisk: Boolean
)