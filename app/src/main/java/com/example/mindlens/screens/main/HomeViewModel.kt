package com.example.mindlens.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.repositories.DiaryRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

// --- DATA CLASS PENDUKUNG ---

data class WeeklyData(
    val day: String,   // Contoh: "Mon", "Tue"
    val score: Float,  // 0.0 sampai 1.0
    val color: Color   // Warna bar
)

data class HomeUiState(
    val entries: List<DiaryEntry> = emptyList(),
    val weeklyStats: List<WeeklyData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val averageMood: String = "Belum ada data"
)

sealed class HomeUiEvent {
    object SaveSuccess : HomeUiEvent()
    data class ShowMessage(val message: String) : HomeUiEvent()
}

// --- VIEW MODEL ---

class HomeViewModel(private val repository: DiaryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = Channel<HomeUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadEntries()
    }

    fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val rawEntries = repository.getMyEntries()

                // --- PERBAIKAN 1: SORTING DESCENDING (Terbaru Paling Atas) ---
                // Kita sort berdasarkan string ISO tanggalnya
                val sortedEntries = rawEntries.sortedByDescending { it.createdAt }

                val weeklyData = calculateWeeklyStats(sortedEntries)
                val avgMoodString = calculateAverageMoodString(sortedEntries)

                _uiState.update {
                    it.copy(
                        entries = sortedEntries, // Gunakan list yang sudah di-sort
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

    // --- TAMBAHAN BARU: FUNGSI UPDATE ---
    fun updateDiaryEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.updateDiaryEntry(entry)
                loadEntries() // Refresh list
                sendEvent(HomeUiEvent.SaveSuccess)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(HomeUiEvent.ShowMessage("Gagal update: ${e.message}"))
            }
        }
    }

    // --- TAMBAHAN BARU: FUNGSI DELETE ---
    fun deleteDiaryEntry(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                repository.deleteDiaryEntry(id)
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
                val currentUserId = repository.getCurrentUserId()
                    ?: throw Exception("User session not found. Please login again.")

                val now = Date()

                val titleFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val autoTitleDate = titleFormatter.format(now)

                val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                isoFormatter.timeZone = TimeZone.getTimeZone("UTC")
                val timestamp = isoFormatter.format(now)

                val generatedId = UUID.randomUUID().toString()

                // --- LOGIKA LOKASI (PERSEMPIT RADIUS) ---

                // 1. Titik Pusat (UMN)
                val centerLat = -6.2572
                val centerLng = 106.6183

                // 2. Offset Kecil (Sekitar 100-200 meter saja)
                // (Math.random() - 0.5) = -0.5 s/d 0.5
                // Dikali 0.002 = Pergeseran maks +/- 0.001 derajat (sekitar 110 meter)
                val spreadFactor = 0.002

                val offsetLat = (Math.random() - 0.5) * spreadFactor
                val offsetLng = (Math.random() - 0.5) * spreadFactor

                val scatteredLat = centerLat + offsetLat
                val scatteredLng = centerLng + offsetLng

                val newEntry = DiaryEntry(
                    id = generatedId,
                    userId = currentUserId,
                    title = title.ifBlank { "Diary - $autoTitleDate" },
                    content = content,
                    mood = mood,
                    color = colorInt.toLong(),
                    createdAt = timestamp,
                    // KOORDINAT BARU (LEBIH RAPAT)
                    latitude = scatteredLat,
                    longitude = scatteredLng
                )

                repository.createDiaryEntry(newEntry)
                loadEntries()
                sendEvent(HomeUiEvent.SaveSuccess)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                sendEvent(HomeUiEvent.ShowMessage("Error: ${e.message}"))
            }
        }
    }

    // --- GANTI FUNGSI calculateWeeklyStats DENGAN INI ---
    private fun calculateWeeklyStats(entries: List<DiaryEntry>): List<WeeklyData> {
        val stats = mutableListOf<WeeklyData>()

        // Formatter standar yang akan kita gunakan setelah string "dibersihkan"
        // Kita paksa semua string jadi format ini nanti
        val standardParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)
        standardParser.timeZone = TimeZone.getTimeZone("UTC")

        val dayLabelFormatter = SimpleDateFormat("EEE", Locale.getDefault())

        val calendar = Calendar.getInstance()
        val loopCal = Calendar.getInstance()
        loopCal.add(Calendar.DAY_OF_YEAR, -6)

        for (i in 0..6) {
            val dateBeingChecked = loopCal.time

            val entriesForDay = entries.filter { entry ->
                // PANGGIL FUNGSI PEMBERSIH DI SINI
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

    // --- FUNGSI BARU: PEMBERSIH STRING TANGGAL (PENTING!) ---
    private fun normalizeDateString(dateString: String): String {
        var clean = dateString

        // 1. Ganti timezone "+00:00" atau "Z" menjadi "+0000" (Format standar SimpleDateFormat)
        if (clean.endsWith("Z")) {
            clean = clean.replace("Z", "+0000")
        } else if (clean.endsWith("+00:00")) {
            clean = clean.replace("+00:00", "+0000")
        }

        // 2. Handle Milidetik
        // Pisahkan bagian tanggal+jam dengan timezone
        // Contoh input: "2025-12-07T10:36:16.532091+0000"
        val parts = clean.split("+")
        if (parts.size == 2) {
            var dateTimePart = parts[0] // "2025-12-07T10:36:16.532091"
            val timezonePart = "+" + parts[1] // "+0000"

            if (dateTimePart.contains(".")) {
                // Punya milidetik
                val splitTime = dateTimePart.split(".")
                var millis = splitTime[1] // "532091" atau "697"

                // Potong jika lebih dari 3 digit, atau tambah 0 jika kurang
                if (millis.length > 3) {
                    millis = millis.substring(0, 3)
                } else {
                    while (millis.length < 3) {
                        millis += "0"
                    }
                }
                dateTimePart = "${splitTime[0]}.$millis"
            } else {
                // Tidak punya milidetik, tambahkan .000
                dateTimePart += ".000"
            }

            // Gabungkan kembali: "2025-12-07T10:36:16.532+0000"
            return dateTimePart + timezonePart
        }

        return clean // Kembalikan apa adanya jika format aneh (biar ditangani try-catch di atas)
    }

    // Helper isSameDay tetap sama
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    // --- HELPER LAINNYA ---

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
            score >= 0.8f -> Color(0xFF64B5F6) // Great (Biru)
            score >= 0.6f -> Color(0xFFAED581) // Good (Hijau Muda)
            score >= 0.4f -> Color(0xFFFFF176) // Neutral (Kuning)
            score >= 0.2f -> Color(0xFFFFB74D) // Bad (Oranye)
            else -> Color(0xFFE57373)          // Awful (Merah)
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
                val repository = DiaryRepository()
                HomeViewModel(repository)
            }
        }
    }
}