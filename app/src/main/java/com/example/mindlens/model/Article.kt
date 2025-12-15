package com.example.mindlens.model

import android.os.Parcelable
import com.example.mindlens.dataClass.Source
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// representing article data as object
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
