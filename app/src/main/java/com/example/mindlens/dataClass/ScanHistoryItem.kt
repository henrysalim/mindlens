package com.example.mindlens.dataClass

import android.graphics.Bitmap
import android.net.Uri
import java.util.UUID

// data structure for scan history item
data class ScanHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: Uri?,
    val bitmap: Bitmap? = null,
    val result: String,
    val confidencePercent: Float,
    val date: String
)