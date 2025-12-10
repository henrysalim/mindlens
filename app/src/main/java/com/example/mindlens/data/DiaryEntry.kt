package com.example.mindlens.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaryEntry(
    // Mencocokkan kolom "id" di Supabase
    @SerialName("id")
    val id: String,

    // WAJIB: Mencocokkan kolom "user_id" yang Not Null di Supabase
    @SerialName("user_id")
    val userId: String,

    @SerialName("title")
    val title: String,

    @SerialName("content")
    val content: String,

    @SerialName("mood")
    val mood: String,

    // Supabase int8 = Kotlin Long (menggunakan Int bisa, tapi Long lebih aman untuk int8)
    @SerialName("color")
    val color: Long,

    // Mencocokkan kolom "created_at"
    @SerialName("created_at")
    val createdAt: String,

    @SerialName("latitude") val latitude: Double? = null,  // Nullable jika user matikan GPS

    @SerialName("longitude") val longitude: Double? = null
)