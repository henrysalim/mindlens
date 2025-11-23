package com.example.projectwithcompose.screens.main

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.projectwithcompose.ui.MoodGreat
import com.example.projectwithcompose.ui.MoodNeutral
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeViewModel : ViewModel() {

    // Gunakan mutableStateListOf di dalam ViewModel agar UI otomatis update
    // Data ini TIDAK AKAN HILANG saat di-rotate
    private val _diaryHistory = mutableStateListOf(
        DiaryEntry(moodLabel = "Happy", note = "Finished the big project!", date = "10:30 AM", color = MoodGreat),
        DiaryEntry(moodLabel = "Neutral", note = "Just a regular day.", date = "Yesterday", color = MoodNeutral)
    )

    // Expose sebagai List biasa (read-only) ke UI agar aman
    val diaryHistory: List<DiaryEntry> get() = _diaryHistory

    // Fungsi untuk menambah diary
    fun addDiaryEntry(mood: String, note: String, color: Color) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        // Tambahkan ke indeks 0 (paling atas)
        _diaryHistory.add(0, DiaryEntry(
            moodLabel = mood,
            note = note,
            date = "Today, $currentTime",
            color = color
        ))
    }
}