package com.example.mindlens.helpers

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDate(date: String?): String {
    val utcDateTime = LocalDateTime.parse(date)

    // 2. Assign UTC zone, then convert to UTC+7 (Asia/Jakarta)
    val targetZonedTime = utcDateTime
        .atZone(ZoneOffset.UTC)
        .withZoneSameInstant(ZoneId.of("Asia/Jakarta"))

    // 3. Format the output
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
    val result = targetZonedTime.format(formatter)

    return result
}