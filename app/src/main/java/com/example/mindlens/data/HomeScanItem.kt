package com.example.mindlens.data

data class HomeScanItem(
    val result: String,
    val date: String,
    val confidence: Int,
    val isRisk: Boolean
)