package com.example.mindlens.helpers

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun formatDiaryDate(timestamp: String?): String {
    // 1. Handle null or empty case
    if (timestamp.isNullOrEmpty()) return "Just now"

    return try {
        // 2. Parse the ISO string from Supabase (e.g., 2023-12-04T10:15:30Z)
        val instant = Instant.parse(timestamp)

        // 3. Convert to the user's local timezone (Critical!)
        val zoneId = ZoneId.systemDefault()
        val zonedDateTime = instant.atZone(zoneId)

        // 4. Create the formatter with your specific pattern
        // "dd"   = Day (04)
        // "MMM"  = Short Month Name (Dec) - "M" alone gives number (12)
        // "yyyy" = Year (2025)
        // "HH"   = 24-hour format (17). Use "hh" for 12-hour.
        // "mm"   = Minutes (15)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm", Locale.getDefault())

        // 5. Format the time
        zonedDateTime.format(formatter)

    } catch (e: Exception) {
        // Fallback if parsing fails
        timestamp
    }
}