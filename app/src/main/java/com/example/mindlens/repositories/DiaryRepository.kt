package com.example.mindlens.repositories

import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.supabase.DatabaseConnection // Pastikan import DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class DiaryRepository {

    private val supabase = DatabaseConnection.supabase

    suspend fun getMyEntries(): List<DiaryEntry> {
        val userId = getCurrentUserId()
        return supabase.from("diary_entries")
            .select {
                if (userId != null) {
                    filter { eq("user_id", userId) }
                }
                // Kita akan melakukan sorting di ViewModel saja agar lebih aman
            }
            .decodeList<DiaryEntry>()
    }

    suspend fun createDiaryEntry(entry: DiaryEntry) {
        supabase.from("diary_entries").insert(entry)
    }

    // --- TAMBAHAN BARU: UPDATE ---
    suspend fun updateDiaryEntry(entry: DiaryEntry) {
        supabase.from("diary_entries").update(entry) {
            filter { eq("id", entry.id) } // Cari berdasarkan ID
        }
    }

    // --- TAMBAHAN BARU: DELETE ---
    suspend fun deleteDiaryEntry(id: String) {
        supabase.from("diary_entries").delete {
            filter { eq("id", id) } // Hapus berdasarkan ID
        }
    }

    fun getCurrentUserId(): String? {
        val user = supabase.auth.currentUserOrNull()
        return user?.id
    }
}