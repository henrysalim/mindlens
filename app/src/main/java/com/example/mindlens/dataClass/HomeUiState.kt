package com.example.mindlens.dataClass

import com.example.mindlens.model.DiaryEntry

// Data structure for storing various states in home screen
data class HomeUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val weeklyStats: List<WeeklyData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val averageMood: String = "Belum ada data",
    val recentScans: List<ScanHistoryItem> = emptyList()
)
