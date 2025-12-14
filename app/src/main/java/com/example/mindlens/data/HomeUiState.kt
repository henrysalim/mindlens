package com.example.mindlens.data

import com.example.mindlens.model.DiaryEntry

data class HomeUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val weeklyStats: List<WeeklyData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val averageMood: String = "Belum ada data"
)