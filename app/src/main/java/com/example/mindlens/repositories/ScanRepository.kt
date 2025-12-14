package com.example.mindlens.repositories

import com.example.mindlens.data.ScanEntry
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class ScanInsert(
    @SerialName("user_id") val userId: String,
    val result: String,
    val confidence: Float
)

class ScanRepository {

    suspend fun saveScan(result: String, confidence: Float) {
        val userId = DatabaseConnection.supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User belum login")


        val payload = ScanInsert(
            userId = userId,
            result = result,
            confidence = confidence
        )

        DatabaseConnection.supabase
            .from("detection_histories")
            .insert(payload)
    }

    suspend fun getMyScans(): List<ScanEntry> {
        val userId = DatabaseConnection.supabase.auth.currentUserOrNull()?.id ?: return emptyList()

        return DatabaseConnection.supabase
            .from("detection_histories")
            .select {
                filter { eq("user_id", userId) }
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<ScanEntry>()
    }

    suspend fun deleteScanById(id: String) {
        // optional guard: pastikan login
        DatabaseConnection.supabase.auth.currentUserOrNull()
            ?: throw IllegalStateException("User belum login")

        DatabaseConnection.supabase
            .from("detection_histories")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }

    suspend fun deleteAllMyScans() {
        val userId = DatabaseConnection.supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User belum login")

        DatabaseConnection.supabase
            .from("detection_histories")
            .delete {
                filter {
                    eq("user_id", userId)
                }
            }
    }
}
