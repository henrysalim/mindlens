package com.example.mindlens.viewModels

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mindlens.data.HomeUiState
import com.example.mindlens.data.WeeklyData
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.repositories.DiaryRepository
import com.example.mindlens.repositories.ScanRepository
import com.example.mindlens.screens.depressionClassifier.ScanHistoryItem
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
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

sealed class HomeUiEvent {
    object SaveSuccess : HomeUiEvent()
    data class ShowMessage(val message: String) : HomeUiEvent()
}

class HomeViewModel(
    private val repository: DiaryRepository,
    private val scanRepository: ScanRepository // Tambahan untuk Scan
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadAllData() // Panggil loadAllData saat ViewModel dibuat
    }

    // --- FUNGSI UTAMA LOAD DATA (Diary + Scan) ---
    fun loadAllData() {
        loadEntries()
        loadRecentScans()
    }

    // --- LOGIKA LOAD SCANS (SUPABASE) ---
    fun loadRecentScans() {
        viewModelScope.launch {
            try {
                // Ambil data scan dari Supabase via Repository
                val scans = withContext(Dispatchers.IO) {
                    scanRepository.getMyScans()
                }

                // Format ke ScanHistoryItem (UI Model) & Ambil 3 teratas
                val formattedScans = scans.take(3).map { e ->
                    ScanHistoryItem(
                        id = e.id ?: "",
                        imageUri = null,
                        bitmap = null,
                        result = e.result,
                        confidencePercent = (e.confidence * 100f).coerceIn(0f, 100f),
                        date = formatScanDate(e.createdAt)
                    )
                }

                _uiState.update { it.copy(recentScans = formattedScans) }
            } catch (e: Exception) {
                // Error silent agar tidak mengganggu flow utama Diary
                e.printStackTrace()
            }
        }
    }

    // Helper format tanggal Scan (menggunakan java.time agar lebih robust terhadap ISO 8601)
    private fun formatScanDate(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return "-"
        val outFmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.getDefault())
        return try {
            val inst = Instant.parse(createdAt)
            inst.atZone(ZoneId.systemDefault()).format(outFmt)
        } catch (_: Exception) {
            createdAt.take(16).replace('T', ' ')
        }
    }

    // --- LOGIKA DIARY (YANG SUDAH ADA) ---
    fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rawEntries = withContext(Dispatchers.IO) { repository.getMyEntries() }

                // Sorting Descending (Terbaru Paling Atas)
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
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Gagal memuat: ${e.message}")
                }
            }
        }
    }

    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) { repository.updateDiaryEntry(entry) }
                loadEntries() // Refresh list
                sendEvent(HomeUiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(HomeUiEvent.ShowMessage("Gagal update: ${e.message}"))
            }
        }
    }

    fun deleteDiaryEntry(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) { repository.deleteDiaryEntry(id) }
                loadEntries() // Refresh list
                sendEvent(HomeUiEvent.ShowMessage("Diary berhasil dihapus"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(HomeUiEvent.ShowMessage("Gagal hapus: ${e.message}"))
            }
        }
    }

    fun saveDiaryEntry(title: String, content: String, mood: String, colorInt: Int, latitude: Double?, longitude: Double?) {
        if (content.isBlank()) {
            sendEvent(HomeUiEvent.ShowMessage("Isi diary tidak boleh kosong"))
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUserId = withContext(Dispatchers.IO) { repository.getCurrentUserId() }
                    ?: throw Exception("User session not found. Please login again.")

                val now = Date()
                val titleFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val autoTitleDate = titleFormatter.format(now)
                val timestamp = java.time.Instant.now().toString()
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

                withContext(Dispatchers.IO) { repository.createDiaryEntry(newEntry) }
                loadEntries()
                sendEvent(HomeUiEvent.SaveSuccess)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(HomeUiEvent.ShowMessage("Error: ${e.message}"))
            }
        }
    }

    private fun calculateWeeklyStats(entries: List<DiaryEntry>): List<WeeklyData> {
        val stats = mutableListOf<WeeklyData>()

        val standardParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        standardParser.timeZone = TimeZone.getTimeZone("UTC")

        val dayLabelFormatter = SimpleDateFormat("EEE", Locale.getDefault())

        val loopCal = Calendar.getInstance()
        loopCal.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dateBeingChecked = loopCal.time

            val entriesForDay = entries.filter { entry ->
                val normalizedString = normalizeDateString(entry.createdAt)
                try {
                    val entryDate = standardParser.parse(normalizedString)
                    if (entryDate != null) {
                        isSameDay(entryDate, dateBeingChecked)
                    } else false
                } catch (e: Exception) {
                    false
                }
            }

            if (entriesForDay.isNotEmpty()) {
                val totalScore = entriesForDay.map { getMoodScore(it.mood) }.sum()
                val avgScore = totalScore / entriesForDay.size

                stats.add(WeeklyData(
                    day = dayLabelFormatter.format(dateBeingChecked),
                    score = avgScore,
                    color = getMoodColor(avgScore)
                ))
            } else {
                stats.add(WeeklyData(
                    day = dayLabelFormatter.format(dateBeingChecked),
                    score = 0f,
                    color = Color.LightGray
                ))
            }
            loopCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return stats
    }

    private fun normalizeDateString(dateString: String): String {
        var clean = dateString
        if (clean.endsWith("Z")) {
            clean = clean.replace("Z", "+0000")
        } else if (clean.endsWith("+00:00")) {
            clean = clean.replace("+00:00", "+0000")
        }

        val parts = clean.split("+")
        if (parts.size == 2) {
            var dateTimePart = parts[0]
            val timezonePart = "+" + parts[1]

            if (dateTimePart.contains(".")) {
                val splitTime = dateTimePart.split(".")
                var millis = splitTime[1]
                if (millis.length > 3) {
                    millis = millis.substring(0, 3)
                } else {
                    while (millis.length < 3) millis += "0"
                }
                dateTimePart = "${splitTime[0]}.$millis"
            } else {
                dateTimePart += ".000"
            }
            return dateTimePart + timezonePart
        }
        return clean
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
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
        if (entries.isEmpty()) return "Belum ada data"
        val totalScore = entries.map { getMoodScore(it.mood) }.sum()
        val avg = totalScore / entries.size
        return when {
            avg >= 0.8f -> "Sangat Bahagia! 🤩"
            avg >= 0.6f -> "Cukup Bahagia 🙂"
            avg >= 0.4f -> "Netral / Biasa 😐"
            avg >= 0.2f -> "Kurang Semangat ☁"
            else -> "Sedang Sedih 😢"
        }
    }

    private fun sendEvent(event: HomeUiEvent) {
        viewModelScope.launch { _uiEvent.send(event) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Inisialisasi Repository di sini
                val diaryRepository = DiaryRepository()
                val scanRepository = ScanRepository()
                HomeViewModel(diaryRepository, scanRepository)
            }
        }
    }
}