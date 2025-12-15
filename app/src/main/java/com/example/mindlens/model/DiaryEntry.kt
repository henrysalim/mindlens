package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// representing diary data as object
@Serializable
data class DiaryEntry(
    @SerialName("id")
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("title")
    val title: String,

    @SerialName("content")
    val content: String,

    @SerialName("mood")
    val mood: String,

    @SerialName("color") // changed the color to Long type in order to store it to database (Supabase)
    val color: Long,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("latitude") val latitude: Double? = null,  // Nullable jika user matikan GPS

    @SerialName("longitude") val longitude: Double? = null
)