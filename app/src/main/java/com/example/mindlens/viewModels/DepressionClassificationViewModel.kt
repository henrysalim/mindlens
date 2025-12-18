package com.example.mindlens.viewModels

import androidx.lifecycle.viewModelScope
import com.example.mindlens.dataClass.ScanHistoryItem
import com.example.mindlens.helpers.DepressionClassifier
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.repositories.ScanRepository
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.mindlens.helpers.formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class DepressionClassifierViewModel : ViewModel() {
    private val repository = ScanRepository()
    private lateinit var classifier: DepressionClassifier

    // UI States
    private val _scanHistory = MutableStateFlow<List<ScanHistoryItem>>(emptyList())
    val scanHistory: StateFlow<List<ScanHistoryItem>> = _scanHistory.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisResult = MutableStateFlow<ScanHistoryItem?>(null)
    val analysisResult: StateFlow<ScanHistoryItem?> = _analysisResult.asStateFlow()

    // Image Selection State
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

    // Toast Message State (Simple event handling)
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()
    val _isToastVisible = MutableStateFlow(false)
    val isToastVisible: StateFlow<Boolean> = _isToastVisible.asStateFlow()


    fun initClassifier(context: Context) {
        if (!::classifier.isInitialized) {
            classifier = DepressionClassifier(context)
        }
    }

    fun onImageSelected(uri: Uri) {
        _selectedImageUri.value = uri
        _capturedBitmap.value = null
        _analysisResult.value = null
    }

    fun onImageCaptured(bitmap: Bitmap) {
        _capturedBitmap.value = bitmap
        _selectedImageUri.value = null
        _analysisResult.value = null
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            try {
                val scans = withContext(Dispatchers.IO) { repository.getMyScans() }
                _scanHistory.value = scans.map { e ->
                    ScanHistoryItem(
                        id = e.id,
                        result = e.result,
                        confidencePercent = (e.confidence * 100f).coerceIn(0f, 100f),
                        date = formatDate(e.createdAt),
                        imageUri = null
                    )
                }
            } catch (e: Exception) {
                Log.e("ERR_LOAD_HIST", e.message.toString())
                _toastMessage.value = "Failed loading recent scans!"
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    fun analyzeImage(context: Context) {
        val uri = _selectedImageUri.value
        val bitmap = _capturedBitmap.value

        if (uri == null && bitmap == null) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val inputBitmap = withContext(Dispatchers.IO) {
                    when {
                        bitmap != null -> bitmap
                        uri != null -> ImageUtils.decodeBitmapFromUri(context, uri)
                        else -> null
                    }
                }

                if (inputBitmap == null) {
                    _toastMessage.value = "Failed to process image"
                    return@launch
                }

                // 1) Inference
                // Ensure classifier is initialized
                if (!::classifier.isInitialized) classifier = DepressionClassifier(context)

                val cls = withContext(Dispatchers.Default) { classifier.classify(inputBitmap) }
                val confidencePercent = (cls.confidence * 100f).coerceIn(0f, 100f)

                val timestamp = java.time.Instant.now().toString()

                // 2) Update Result State
                val newItem = ScanHistoryItem(
                    id = UUID.randomUUID().toString(),
                    imageUri = selectedImageUri.value,
                    bitmap = capturedBitmap.value,
                    result = cls.label,
                    confidencePercent = confidencePercent,
                    date = timestamp
                )
                _analysisResult.value = newItem

                // 3) Save to DB
                withContext(Dispatchers.IO) {
                    repository.saveScan(cls.label, cls.confidence)
                }

                // 4) Refresh History
                loadHistory()

            } catch (e: Exception) {
                Log.e("ERR_ANALYZE_IMG", e.message.toString())
                _toastMessage.value = "Failed to analyze image!"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun deleteScan(id: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.deleteScanById(id) }
                loadHistory()
                if (_analysisResult.value?.id == id) _analysisResult.value = null
                _isToastVisible.value = true
                _toastMessage.value = "Scan deleted successfully!"
            } catch (e: Exception) {
                Log.e("ERR_DELETE_SCAN", e.message.toString())
                _toastMessage.value = "Failed to delete scan!"
            }
        }
    }

    fun deleteAllScans() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { repository.deleteAllMyScans() }
                _scanHistory.value = emptyList() // Clear immediately for responsiveness
                _analysisResult.value = null
                _isToastVisible.value = true
                _toastMessage.value = "Recent Scans deleted successfully!"
            } catch (e: Exception) {
                Log.e("ERR_DEL_ALL_SCANS", e.message.toString())
                _toastMessage.value = "Failed to delete recent scans!"
            }
        }
    }
}