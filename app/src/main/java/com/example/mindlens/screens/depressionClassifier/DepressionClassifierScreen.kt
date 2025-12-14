package com.example.mindlens.screens.depressionClassifier

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.mindlens.helpers.DepressionClassifier
import com.example.mindlens.repositories.ScanRepository
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// --- UI MODEL (buat tampilan list) ---
data class ScanHistoryItem(
    val id: String = UUID.randomUUID().toString(), // harus id DB kalau mau delete akurat
    val imageUri: Uri?,
    val bitmap: Bitmap? = null,
    val result: String,
    val confidencePercent: Float, // 0..100
    val date: String
)

@Composable
fun DepressionClassifierScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repository = remember { ScanRepository() }
    val classifier = remember { DepressionClassifier(context) }

    // --- STATE UI ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ScanHistoryItem?>(null) }

    // --- HISTORY (dari Supabase) ---
    val scanHistory = remember { mutableStateListOf<ScanHistoryItem>() }
    var isHistoryLoading by remember { mutableStateOf(true) }

    // --- DELETE STATE ---
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    // Helper: refresh history dari Supabase
    fun refreshHistory() {
        scope.launch {
            isHistoryLoading = true
            try {
                val scans = withContext(Dispatchers.IO) { repository.getMyScans() }
                scanHistory.clear()
                scanHistory.addAll(
                    scans.map { e ->
                        ScanHistoryItem(
                            id = e.id ?: UUID.randomUUID().toString(),
                            imageUri = null,
                            bitmap = null,
                            result = e.result,
                            confidencePercent = (e.confidence * 100f).coerceIn(0f, 100f), // DB simpan 0..1
                            date = formatScanDate(e.createdAt)
                        )
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal load history: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isHistoryLoading = false
            }
        }
    }

    LaunchedEffect(Unit) { refreshHistory() }

    // --- LAUNCHERS ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            capturedBitmap = null
            analysisResult = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            capturedBitmap = it
            selectedImageUri = null
            analysisResult = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
    }

    fun decodeBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val raw = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            }
            raw.copy(Bitmap.Config.ARGB_8888, true)
        } catch (_: Exception) {
            null
        }
    }

    fun analyzeImage() {
        if (selectedImageUri == null && capturedBitmap == null) return

        scope.launch {
            isAnalyzing = true
            try {
                val inputBitmap = withContext(Dispatchers.IO) {
                    when {
                        capturedBitmap != null -> capturedBitmap
                        selectedImageUri != null -> decodeBitmapFromUri(selectedImageUri!!)
                        else -> null
                    }
                }

                if (inputBitmap == null) {
                    Toast.makeText(context, "Gagal membaca gambar", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 1) Inference
                val cls = withContext(Dispatchers.IO) { classifier.classify(inputBitmap) }
                val confidencePercent = (cls.confidence * 100f).coerceIn(0f, 100f)

                val nowLabel = ZonedDateTime.now(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.getDefault()))

                // 2) Update UI result (optimistic)
                analysisResult = ScanHistoryItem(
                    id = UUID.randomUUID().toString(), // sementara (nanti refresh ambil id asli)
                    imageUri = selectedImageUri,
                    bitmap = capturedBitmap,
                    result = cls.label,
                    confidencePercent = confidencePercent,
                    date = nowLabel
                )

                // 3) Save ke Supabase (DB simpan 0..1)
                withContext(Dispatchers.IO) {
                    repository.saveScan(cls.label, cls.confidence)
                }

                // 4) Refresh history biar id-nya valid untuk delete
                refreshHistory()

            } catch (e: Exception) {
                Toast.makeText(context, "Analyze error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isAnalyzing = false
            }
        }
    }

    // --- CONFIRM DIALOG: delete single ---
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Hapus riwayat?") },
            text = { Text("Riwayat scan ini akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = pendingDeleteId!!
                        pendingDeleteId = null
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) { repository.deleteScanById(id) }
                                // refresh biar sinkron dengan server
                                refreshHistory()
                                if (analysisResult?.id == id) analysisResult = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal hapus: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Hapus") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Batal") }
            }
        )
    }

    // --- CONFIRM DIALOG: clear all ---
    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title = { Text("Hapus semua riwayat?") },
            text = { Text("Semua riwayat scan akan dihapus permanen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAllConfirm = false
                        scope.launch {
                            try {
                                withContext(Dispatchers.IO) { repository.deleteAllMyScans() }
                                scanHistory.clear()
                                analysisResult = null
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gagal hapus semua: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) { Text("Hapus Semua") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Batal") }
            }
        )
    }

    // --- UI LAYOUT ---
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBackground)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI Diagnostic",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                    Text(
                        "Analyze facial expressions using Deep Learning.",
                        color = TechTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "On-Device",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(
                        2.dp,
                        if (isAnalyzing) TechPrimary else Color.LightGray.copy(0.3f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    capturedBitmap != null -> {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Face, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Image Selected", color = Color.LightGray)
                        }
                    }
                }

                if (isAnalyzing) ScanningEffect()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.Image, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { analyzeImage() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White),
                enabled = (selectedImageUri != null || capturedBitmap != null) && !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing Face...")
                } else {
                    Icon(Icons.Default.Analytics, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Diagnosis")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Result
        item {
            AnimatedVisibility(
                visible = analysisResult != null,
                enter = slideInVertically() + fadeIn()
            ) {
                analysisResult?.let { ResultDashboard(it) }
            }
        }

        // History header + Clear All
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = { showDeleteAllConfirm = true },
                    enabled = scanHistory.isNotEmpty() && !isHistoryLoading
                ) {
                    Text("Clear All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isHistoryLoading && scanHistory.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TechPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Loading history...", color = TechTextSecondary, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (!isHistoryLoading && scanHistory.isEmpty()) {
                Text("Belum ada riwayat scan.", color = TechTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // History list (dengan delete per item)
        items(scanHistory, key = { it.id }) { history ->
            HistoryItemCard(
                item = history,
                onDeleteClick = { id -> pendingDeleteId = id }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun ScanningEffect() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanAnim")
    val yPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = yPosition.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, TechPrimary, Color.Transparent)
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = TechPrimary.copy(alpha = 0.1f),
                topLeft = Offset(0f, 0f),
                size = size
            )
        }
    }
}

@Composable
fun ResultDashboard(result: ScanHistoryItem) {
    val isPositive = result.result.contains("Depresi", ignoreCase = true) || result.result == "1"
    val color = if (isPositive) Color(0xFFEF5350) else Color(0xFF66BB6A)
    val icon = if (isPositive) Icons.Default.Warning else Icons.Default.CheckCircle

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                result.result,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "Confidence: ${"%.1f".format(result.confidencePercent)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = TechTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Recommended Action:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            if (isPositive) {
                RecommendationItem(Icons.Default.Call, "Call Helpline", "Talk to a professional now.", Color(0xFFFFF3E0))
                Spacer(modifier = Modifier.height(8.dp))
                RecommendationItem(Icons.Default.SelfImprovement, "Try Meditation", "Calm your mind with guided audio.", Color(0xFFE1F5FE))
            } else {
                RecommendationItem(Icons.Default.SentimentSatisfied, "Keep it up!", "Maintain your good mood.", Color(0xFFE8F5E9))
            }
        }
    }
}

@Composable
fun RecommendationItem(icon: ImageVector, title: String, desc: String, bgColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TechTextPrimary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(desc, fontSize = 12.sp, color = TechTextSecondary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ArrowForwardIos, null, modifier = Modifier.size(14.dp), tint = TechTextSecondary)
    }
}

@Composable
fun HistoryItemCard(
    item: ScanHistoryItem,
    onDeleteClick: (String) -> Unit
) {
    val isRisk = item.result.contains("Depresi", ignoreCase = true) || item.result == "1"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.History, null, tint = TechTextSecondary)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.result,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isRisk) Color(0xFFEF5350) else Color(0xFF2E7D32)
                )
                Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
            }

            Text("${item.confidencePercent.toInt()}%", fontWeight = FontWeight.Bold, color = TechPrimary)

            IconButton(onClick = { onDeleteClick(item.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}


private fun formatScanDate(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) return "-"
    val outFmt = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale.getDefault())

    return try {
        val odt = OffsetDateTime.parse(createdAt)
        odt.atZoneSameInstant(ZoneId.systemDefault()).format(outFmt)
    } catch (_: Exception) {
        try {
            val inst = Instant.parse(createdAt)
            inst.atZone(ZoneId.systemDefault()).format(outFmt)
        } catch (_: Exception) {
            createdAt.take(16).replace('T', ' ')
        }
    }
}
