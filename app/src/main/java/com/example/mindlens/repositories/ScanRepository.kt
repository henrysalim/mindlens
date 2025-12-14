package com.example.mindlens.repositories

import com.example.mindlens.data.ScanEntry
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ScanRepository {

    // 1. Simpan hasil scan ke Supabase
    suspend fun saveScan(result: String, confidence: Float) {
        val entry = ScanEntry(
            result = result,
            confidence = confidence

        )
        DatabaseConnection.supabase.from("detection_histories").insert(entry)
    }

    // 2. Ambil riwayat scan user
    suspend fun getMyScans(): List<ScanEntry> {
        return DatabaseConnection.supabase.from("detection_histories")
            .select {
                order("created_at", order = Order.DESCENDING) // Urutkan dari yang terbaru
            }
            .decodeList<ScanEntry>()
    }
}