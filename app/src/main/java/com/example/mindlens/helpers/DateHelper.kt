package com.example.mindlens.helpers

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun formatDiaryDate(dateString: String): String {
    if (dateString.isBlank()) return "Unknown Date"

    return try {
        // 1. Format Supabase (UTC) -> "2025-12-07T10:00:00Z"
        // Kita gunakan pola ini untuk membaca data dari database
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC") // Wajib set ke UTC agar jamnya pas

        val date: Date? = inputFormat.parse(dateString)

        // 2. Format Tampilan (Waktu Lokal HP User) -> "07 Dec 2025, 10:00"
        if (date != null) {
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            // Tidak perlu set timezone, otomatis ikut HP user
            outputFormat.format(date)
        } else {
            dateString
        }
    } catch (e: Exception) {
        // Fallback jika format gagal
        dateString
    }
}