package com.example.mindlens.viewModels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mindlens.dataClass.HomeUiState
import com.example.mindlens.dataClass.ScanHistoryItem
import com.example.mindlens.dataClass.WeeklyData
import com.example.mindlens.helpers.formatDate
import com.example.mindlens.helpers.getLoggedInUserId
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.repositories.DiaryRepository
import com.example.mindlens.repositories.ScanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle

sealed class HomeUiEvent {
    object SaveSuccess : HomeUiEvent()
    data class ShowMessage(val message: String) : HomeUiEvent()
}

class HomeViewModel(
    private val repository: DiaryRepository,
    private val scanRepository: ScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        // load all data when home screen opened
        loadAllData()
    }

    fun loadAllData() {
        loadEntries()
        loadRecentScans()
    }

    fun loadRecentScans() {
        viewModelScope.launch {
            try {
                val scans = withContext(Dispatchers.IO) {
                    scanRepository.getMyScans()
                }

                val formattedScans = scans.take(3).map { e ->
                    ScanHistoryItem(
                        id = e.id ?: "",
                        imageUri = null,
                        bitmap = null,
                        result = e.result,
                        confidencePercent = (e.confidence * 100f).coerceIn(0f, 100f),
                        date = formatDate(e.createdAt)
                    )
                }

                _uiState.update { it.copy(recentScans = formattedScans) }
            } catch (e: Exception) {
                Log.e("ERR_LOAD_RECENT_SCANS", e.message.toString())
            }
        }
    }

    // load entries
    fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rawEntries = withContext(Dispatchers.IO) {
                    repository.getDiaryEntries()
                }

                val sortedEntries = rawEntries.sortedByDescending { it.createdAt }

                val weeklyData = calculateWeeklyStats(sortedEntries)
                val avgMoodString = calculateAverageMoodString(sortedEntries)

                _uiState.update {
                    it.copy(
                        entries = sortedEntries,
                        weeklyStats = weeklyData,
                        averageMood = avgMoodString,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("ERR_LOAD_ENTRIES", e.message.toString())
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Gagal memuat diary!")
                }
            }
        }
    }

    // update diary entry
    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) { repository.updateDiaryEntry(entry) }
                loadEntries()
                sendEvent(HomeUiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Log.e("ERR_UPDATE_DIARY_ENTRY", e.message.toString())
                sendEvent(HomeUiEvent.ShowMessage("Gagal update diary!"))
            }
        }
    }

    fun deleteDiaryEntry(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) { repository.deleteDiaryEntry(id) }
                loadEntries()
                sendEvent(HomeUiEvent.ShowMessage("Diary berhasil dihapus"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Log.e("ERR_DELETE_DIARY_ENTRY", e.message.toString())
                sendEvent(HomeUiEvent.ShowMessage("Gagal hapus diary!"))
            }
        }
    }

    fun saveDiaryEntry(
        title: String,
        content: String,
        mood: String,
        colorInt: Int,
        latitude: Double?,
        longitude: Double?
    ) {
        if (content.isBlank()) {
            sendEvent(HomeUiEvent.ShowMessage("Isi diary tidak boleh kosong"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUserId = getLoggedInUserId()
                    ?: throw Exception("User session not found. Please login again.")

                val now = Date()
                val titleFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val autoTitleDate = titleFormatter.format(now)
                val timestamp = Instant.now().toString()
                val generatedId = UUID.randomUUID().toString()

                fun randomizeLocation(lat: Double, lng: Double, radiusMeters: Double): Pair<Double, Double> {
                    val radiusInDegrees = radiusMeters / 111_320.0
                    val u = Math.random()
                    val v = Math.random()
                    val w = radiusInDegrees * sqrt(u)
                    val t = 2 * PI * v
                    val deltaLat = w * cos(t)
                    val deltaLng = w * sin(t) / cos(Math.toRadians(lat))
                    return Pair(lat + deltaLat, lng + deltaLng)
                }

                val (finalLat, finalLng) = if (latitude != null && longitude != null) {
                    randomizeLocation(latitude, longitude, radiusMeters = 50.0)
                } else {
                    null to null
                }

                val newEntry = DiaryEntry(
                    id = generatedId,
                    userId = currentUserId,
                    title = title.ifBlank { "Diary - $autoTitleDate" },
                    content = content,
                    mood = mood,
                    color = colorInt.toLong(),
                    createdAt = timestamp,
                    latitude = finalLat,
                    longitude = finalLng
                )

                // insert databse with withContext
                withContext(Dispatchers.IO) {
                    repository.createDiaryEntry(newEntry)
                }

                loadEntries()
                sendEvent(HomeUiEvent.SaveSuccess)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                Log.e("ERR_SAVE_DIARY_ENTRY", e.message.toString())
                sendEvent(HomeUiEvent.ShowMessage("Error menyimpan diary!"))
            }
        }
    }

    private fun calculateWeeklyStats(entries: List<DiaryEntry>): List<WeeklyData> {
        val stats = mutableListOf<WeeklyData>()

        // Get phone's Timezone
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)

        // Loop to last 7 days
        for (i in 6 downTo 0) {
            val targetDate = today.minusDays(i.toLong())

            // Filter entry
            val entriesForDay = entries.filter { entry ->
                try {
                    // Parsing ISO-8601
                    val entryInstant = LocalDateTime.parse(entry.createdAt)
                    // Convert to localdate
                    val entryDate = entryInstant.atZone(zoneId).toLocalDate()

                    // Bandingkan apakah harinya sama
                    entryDate.isEqual(targetDate)
                } catch (e: Exception) {
                    Log.e("ERR_CALC_WEEKLY_STATS", e.message.toString())
                    false
                }
            }

            // Format day name
            val dayLabel = targetDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            // Calculate avg score
            if (entriesForDay.isNotEmpty()) {
                val totalScore = entriesForDay.map { getMoodScore(it.mood) }.sum()
                val avgScore = totalScore / entriesForDay.size
                stats.add(WeeklyData(day = dayLabel, score = avgScore, color = getMoodColor(avgScore)))
            } else {
                stats.add(WeeklyData(day = dayLabel, score = 0f, color = Color.LightGray))
            }
        }

        return stats
    }

    private fun getMoodScore(mood: String): Float {
        return when (mood.lowercase()) {
            "great", "amazing", "bahagia" -> 1.0f
            "good", "senang" -> 0.75f
            "neutral", "okay", "biasa" -> 0.5f
            "bad", "buruk" -> 0.25f
            "awful", "terrible", "sedih" -> 0.15f
            else -> 0.5f
        }
    }

    private fun getMoodColor(score: Float): Color {
        return when {
            score >= 0.8f -> Color(0xFF64B5F6)
            score >= 0.6f -> Color(0xFFAED581)
            score >= 0.4f -> Color(0xFFFFF176)
            score >= 0.2f -> Color(0xFFFFB74D)
            else -> Color(0xFFE57373)
        }
    }

    private fun calculateAverageMoodString(entries: List<DiaryEntry>): String {
        if (entries.isEmpty()) return "No data available"
        val totalScore = entries.map { getMoodScore(it.mood) }.sum()
        val avg = totalScore / entries.size
        return when {
            avg >= 0.8f -> "Very Happy! 🤩"
            avg >= 0.6f -> "Happy 🙂"
            avg >= 0.4f -> "Normal 😐"
            avg >= 0.2f -> "Not Happy ☁"
            else -> "Sad 😢"
        }
    }

    fun updateDiaryLocation(diaryId: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            repository.updateDiaryLocation(diaryId, lat, lng)
        }
    }

    private fun sendEvent(event: HomeUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val diaryRepository = DiaryRepository()
                val scanRepository = ScanRepository()
                HomeViewModel(diaryRepository, scanRepository)
            }
        }
    }
}