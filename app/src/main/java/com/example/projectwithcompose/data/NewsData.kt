package com.example.projectwithcompose.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName // Pastikan import ini ada
import kotlinx.parcelize.Parcelize

// Response Utama
data class NewsApiResponse(
    val totalArticles: Int,
    val articles: List<Article>
)

@Parcelize
data class Article(
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,

    // PENTING: Tambahkan @SerializedName("image")
    // Ini memberitahu GSON untuk mengambil data dari field "image" di JSON GNews
    @SerializedName("image")
    val image: String?,

    val publishedAt: String,
    val source: Source?
) : Parcelable

@Parcelize
data class Source(
    val name: String?,
    val url: String?
) : Parcelable