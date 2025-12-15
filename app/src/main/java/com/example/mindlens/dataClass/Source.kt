package com.example.mindlens.dataClass

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// data structure for storing the detail of article's source
@Parcelize
data class Source(
    val name: String?,
    val url: String?
) : Parcelable