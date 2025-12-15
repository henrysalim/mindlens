package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// model to get the related commenter
@Serializable
data class Profile(
    @SerialName("full_name")
    val fullName: String? = "Anonymous",

    @SerialName("avatar")
    val avatar: String? = null
)

// model to represent the comment (GET)
@Serializable
data class GetArticleComment(
    @SerialName("id")
    val id: String,

    @SerialName("comment")
    val comment: String,

    @SerialName("news_url")
    val news_url: String,

    @SerialName("user_id")
    val user_id: String?,

    @SerialName("created_at")
    val createdAt: String?,

    @SerialName("profiles")
    val profile: Profile? = null
)

// model to represent the comment (POST)
@Serializable
data class PostArticleComment(
    @SerialName("id")
    val id: String,

    @SerialName("comment")
    val comment: String,

    @SerialName("news_url")
    val news_url: String,

    @SerialName("user_id")
    val user_id: String?,

    @SerialName("created_at")
    val createdAt: String?,
)
