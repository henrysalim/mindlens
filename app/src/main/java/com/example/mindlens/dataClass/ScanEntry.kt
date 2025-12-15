package com.example.mindlens.dataClass

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// data structure for ML classification result
@Serializable
data class ScanEntry(
    val id: String,
    @SerialName("user_id") val userId: String,
    val result: String,
    val confidence: Float,
    @SerialName("created_at") val createdAt: String
)
