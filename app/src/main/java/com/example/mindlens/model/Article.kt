package com.example.mindlens.model

import android.os.Parcelable
import com.example.mindlens.data.Source
import com.google.gson.annotations.SerializedName // Pastikan import ini ada
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
