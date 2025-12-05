package com.example.mindlens.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryEntry(
    val id: String? = null,
    val title: String,
    val content: String,
    val mood: String,
    val color: Int,
    @SerialName("created_at") val createdAt: String? = null
)