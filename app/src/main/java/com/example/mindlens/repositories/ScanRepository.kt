package com.example.mindlens.repositories

import android.util.Log
import com.example.mindlens.dataClass.ScanEntry
import com.example.mindlens.helpers.getLoggedInUserId
import com.example.mindlens.model.ScanInsert
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ScanRepository {
    private val supabase = DatabaseConnection.supabase
    private val userId = getLoggedInUserId() ?: ""

    suspend fun saveScan(result: String, confidence: Float) {
        val timestamp = java.time.Instant.now().toString()

        val payload = ScanInsert(
            userId = userId,
            result = result,
            confidence = confidence,
            created_at = timestamp
        )

        try {
            supabase.from("detection_histories").insert(payload)
        } catch (e: Exception) {
            Log.e("ScanRepo", "Insert error: ${e.message}")
            throw e // Rethrow to let ViewModel handle UI state
        }
    }

    suspend fun getMyScans(): List<ScanEntry> {
        val userId = getLoggedInUserId() ?: return emptyList()

        return try {
            supabase.from("detection_histories").select {
                filter { eq("user_id", userId) }
                order("created_at", order = Order.DESCENDING)
            }.decodeList<ScanEntry>()
        } catch (e: Exception) {
            Log.e("ScanRepo", "Fetch error: ${e.message}")
            emptyList()
        }
    }

    suspend fun deleteScanById(id: String) {
        try {
            supabase.from("detection_histories").delete {
                filter { eq("id", id) }
            }
        } catch (e: Exception) {
            Log.e("ScanRepo", "Delete error: ${e.message}")
            throw e
        }
    }

    suspend fun deleteAllMyScans() {
        try {
            supabase.from("detection_histories").delete {
                filter { eq("user_id", userId) }
            }
        } catch (e: Exception) {
            Log.e("ScanRepo", "Delete all error: ${e.message}")
            throw e
        }
    }
}
