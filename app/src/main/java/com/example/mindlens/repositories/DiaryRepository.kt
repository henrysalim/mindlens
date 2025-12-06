package com.example.mindlens.repositories

import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class DiaryRepository {
    // specific function to save an entry
    suspend fun createDiaryEntry(entry: DiaryEntry) {
        // "diaries" matches the table name you created in SQL
        DatabaseConnection.supabase.from("diary_entries").insert(entry)
    }

    // specific function to fetch user's entries
    suspend fun getMyEntries(): List<DiaryEntry> {
        return DatabaseConnection.supabase.from("diary_entries")
            .select {
                order("created_at", order = Order.DESCENDING)
            } // selects all columns
            .decodeList<DiaryEntry>() // converts JSON -> List<DiaryEntry>
    }
}