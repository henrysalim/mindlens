package com.example.mindlens.data

import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.screens.depressionClassifier.ScanHistoryItem

data class HomeUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val weeklyStats: List<WeeklyData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val averageMood: String = "Belum ada data",
    val recentScans: List<ScanHistoryItem> = emptyList()
)
