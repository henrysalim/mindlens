package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,

    // Matches DB column: "bio"
    @SerialName("bio")
    val bio: String? = "",

    // Matches DB column: "bio"
    @SerialName("created_at")
    val created_at: String? = "",

    // Matches DB column: "full_name"
    @SerialName("full_name")
    val fullName: String? = "",

    // Matches DB column: "avatar"
    @SerialName("avatar")
    val avatar: String? = null,
)