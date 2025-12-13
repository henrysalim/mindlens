package com.example.mindlens.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleComments(
    @SerialName("id")
    val id: String,

    @SerialName("comment")
    val comment: String,

    @SerialName("news_url")
    val news_url: String,

    @SerialName("created_at")
    val createdAt: String?
)
