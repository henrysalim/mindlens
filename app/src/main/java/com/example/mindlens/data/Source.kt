package com.example.mindlens.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//for storing the detail of article's source
@Parcelize
data class Source(
    val name: String?,
    val url: String?
) : Parcelable