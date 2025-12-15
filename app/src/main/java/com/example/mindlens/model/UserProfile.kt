package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// for representing user's profile data as object
@Serializable
data class UserProfile(
    val id: String,

    @SerialName("bio")
    val bio: String? = "",

    @SerialName("created_at")
    val created_at: String? = "",

    @SerialName("full_name")
    val fullName: String? = "",

    @SerialName("avatar")
    val avatar: String? = null,
)