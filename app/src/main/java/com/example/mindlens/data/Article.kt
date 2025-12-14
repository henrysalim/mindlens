package com.example.mindlens.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Article(
    val title: String,
    val description: String?,
    val content: String?,
    val url: String,

    @SerializedName("image")
    val image: String?,

    val publishedAt: String,
    val source: Source?
) : Parcelable