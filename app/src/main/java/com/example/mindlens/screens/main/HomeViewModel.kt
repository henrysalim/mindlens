package com.example.mindlens.ui

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.repositories.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.time.Instant

class HomeViewModel : ViewModel() {
    private val repository = DiaryRepository()

    // 1. The State Holder for the History
    private val _diaryHistory = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val diaryHistory = _diaryHistory.asStateFlow()

    // 2. Loading State (Optional but recommended)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // 3. Load data when ViewModel starts
    init {
        loadEntries()
    }

    // Function to fetch from Supabase
    fun loadEntries() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val entries = repository.getMyEntries()
                _diaryHistory.value = entries
            } catch (e: Exception) {
                println("Error loading diaries: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Function to save data
    fun saveDiaryEntry(
        content: String,
        mood: String,
        colorInt: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        val autoTitle = "Diary Entry - $currentDate"
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val timestamp = isoFormat.format(Date())

        viewModelScope.launch {
            try {
                val newEntry = DiaryEntry(
                    id = UUID.randomUUID().toString(),
                    title = autoTitle,
                    content = content,
                    mood = mood,
                    color = colorInt,
                    createdAt = timestamp
                )

                repository.createDiaryEntry(newEntry)
                loadEntries() // Refresh list
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}