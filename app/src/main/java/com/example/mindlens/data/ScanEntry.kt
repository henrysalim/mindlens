package com.example.mindlens.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScanEntry(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String? = null,

    val result: String,
    val confidence: Float,

    @SerialName("created_at")
    val createdAt: String? = null
)
