package com.example.projectwithcompose.repositories

import com.example.projectwithcompose.data.DiaryEntry
import com.example.projectwithcompose.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from

class DiaryRepository {
    // specific function to save an entry
    suspend fun createDiaryEntry(entry: DiaryEntry) {
        // "diaries" matches the table name you created in SQL
        DatabaseConnection.supabase.from("diaries").insert(entry)
    }

    // specific function to fetch user's entries
    suspend fun getMyEntries(): List<DiaryEntry> {
        return DatabaseConnection.supabase.from("diaries")
            .select() // selects all columns
            .decodeList<DiaryEntry>() // converts JSON -> List<DiaryEntry>
    }
}