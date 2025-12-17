package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// for representing scan insert from db as object
@Serializable
data class ScanInsert(
    @SerialName("user_id") val userId: String,
    val result: String,
    val confidence: Float,
    val created_at: String
)