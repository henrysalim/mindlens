package com.example.mindlens.repositories

import android.util.Log
import com.example.mindlens.helpers.getLoggedInUserId
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from

class DiaryRepository {
    private val supabase = DatabaseConnection.supabase

    // get diary only for its user
    suspend fun getDiaryEntries(): List<DiaryEntry> {
        val userId = getLoggedInUserId()
        return try {
            supabase.from("diary_entries")
                .select {
                    if (userId != null) {
                        filter { eq("user_id", userId) }
                    }
                }
                .decodeList<DiaryEntry>()
        } catch (e: Exception) {
            Log.e("ERR_GET_DIARY", e.message.toString())
            return emptyList()
        }
    }

    // inserting the diary to database
    suspend fun createDiaryEntry(entry: DiaryEntry) {
        try {
            supabase.from("diary_entries").insert(entry)
        } catch (e: Exception) {
            Log.e("ERR_CREATE_DIARY", e.message.toString())
        }
    }

    // update user's diary
    suspend fun updateDiaryEntry(entry: DiaryEntry) {
        try {
            supabase.from("diary_entries").update(entry) {
                filter { eq("id", entry.id) } // Cari berdasarkan ID
            }
        } catch (e: Exception) {
            Log.e("ERR_UPDATE_DIARY", e.message.toString())
        }
    }

    // Deleting user's diary
    suspend fun deleteDiaryEntry(id: String) {
        try {
            supabase.from("diary_entries").delete {
                filter { eq("id", id) } // Hapus berdasarkan ID
            }
        } catch (e: Exception) {
            Log.e("ERR_DELETE_DIARY", e.message.toString())
        }
    }

    suspend fun updateDiaryLocation(
        diaryId: String,
        latitude: Double,
        longitude: Double
    ) {
        supabase.from("diary_entries").update(
            mapOf(
                "latitude" to latitude,
                "longitude" to longitude
            )
        ) {
            filter { eq("id", diaryId) }
        }
    }
}