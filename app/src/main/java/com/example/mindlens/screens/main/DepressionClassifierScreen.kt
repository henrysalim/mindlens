package com.example.mindlens.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.ui.*
import com.example.mindlens.helpers.DepressionClassifier
import com.example.mindlens.repositories.ScanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// --- DATA MODEL ---
data class ScanHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: Uri?,      // Untuk gambar dari galeri
    val bitmap: Bitmap? = null, // Untuk gambar dari kamera (thumbnail)
    val result: String,
    val confidence: Float,
    val date: String
)

@Composable
fun DepressionClassifierScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val repository = remember { ScanRepository()}

    // --- STATE UI ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) } // Untuk hasil kamera
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ScanHistoryItem?>(null) }

    // --- INISIALISASI PTL CLASSIFIER ---
    // Menggunakan 'remember' agar classifier hanya dibuat sekali
    val classifier = remember { DepressionClassifier(context) }

    // --- HISTORY STATE (DENGAN PLACEHOLDER AWAL) ---
    val scanHistory = remember {
        mutableStateListOf(
            ScanHistoryItem(imageUri = null, result = "Normal / Sehat", confidence = 92f, date = "Hari ini, 08:30"),
            ScanHistoryItem(imageUri = null, result = "Indikasi Depresi", confidence = 74f, date = "Kemarin, 20:15"),
            ScanHistoryItem(imageUri = null, result = "Normal / Sehat", confidence = 88f, date = "5 Des, 14:00"),
            ScanHistoryItem(imageUri = null, result = "Indikasi Depresi", confidence = 81f, date = "1 Des, 09:10")
        )
    }

    // --- LAUNCHERS ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            capturedBitmap = null // Reset kamera jika pilih galeri
            analysisResult = null // Reset hasil lama
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            capturedBitmap = it
            selectedImageUri = null
            analysisResult = null
        }
    }

// [BARU] Launcher untuk Meminta Izin Kamera
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Jika diizinkan, langsung buka kamera
            cameraLauncher.launch(null)
        } else {
            // Jika ditolak, beri tahu user
            Toast.makeText(context, "Izin kamera diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    // --- LOGIKA UTAMA: ANALYZE IMAGE ---
    fun analyzeImage() {
        if (selectedImageUri == null && capturedBitmap == null) return

        scope.launch {
            isAnalyzing = true

            // Pindah ke Background Thread untuk proses berat
            withContext(Dispatchers.IO) {
                try {
                    // 1. Siapkan Bitmap (Entah dari Uri atau Kamera)
                    val inputBitmap: Bitmap? = if (capturedBitmap != null) {
                        capturedBitmap
                    } else if (selectedImageUri != null) {
                        if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                            ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                                decoder.isMutableRequired = true
                            }
                        }
                    } else {
                        null
                    }

                    // 2. Jalankan Klasifikasi PTL
                    if (inputBitmap != null) {
                        try {
                            val result = classifier.classify(inputBitmap)

                            // 3. Update UI di Main Thread
                            withContext(Dispatchers.Main) {
                                val newItem = ScanHistoryItem(
                                    imageUri = selectedImageUri,
                                    bitmap = capturedBitmap,
                                    result = result.label,
                                    confidence = result.confidence * 100, // Konversi 0.9 -> 90%
                                    date = SimpleDateFormat(
                                        "dd MMM, HH:mm",
                                        Locale.getDefault()
                                    ).format(Date())
                                )

                                analysisResult = newItem
                                scanHistory.add(0, newItem) // Tambah ke paling atas
                            }
                        }
                        catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isAnalyzing = false
                }
            }
        }
    }

    // --- UI LAYOUT ---
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBackground)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        // 1. Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Diagnostic", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                    Text("Analyze facial expressions using Deep Learning.", color = TechTextSecondary, fontSize = 12.sp)
                }
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp))
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Shield, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("On-Device", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Image Preview
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(2.dp, if (isAnalyzing) TechPrimary else Color.LightGray.copy(0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (capturedBitmap != null) {
                    // Tampilkan hasil kamera
                    androidx.compose.foundation.Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedImageUri != null) {
                    // Tampilkan hasil galeri
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder Kosong
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Face, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Image Selected", color = Color.LightGray)
                    }
                }

                // Efek Laser saat analyzing
                if (isAnalyzing) {
                    ScanningEffect()
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Control Buttons
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.Image, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedButton(
                    onClick = {
                        // CEK IZIN SEBELUM BUKA KAMERA
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)

                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            // Jika sudah punya izin, buka kamera
                            cameraLauncher.launch(null)
                        } else {
                            // Jika belum, minta izin dulu
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // ANALYZE BUTTON
            Button(
                onClick = { analyzeImage() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
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

        // 4. Result Section
        item {
            AnimatedVisibility(visible = analysisResult != null, enter = slideInVertically() + fadeIn()) {
                analysisResult?.let { result -> ResultDashboard(result) }
            }
        }

        // 5. History List
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text("Recent Scans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(scanHistory) { history ->
            HistoryItemCard(history)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun ScanningEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val yPosition by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 300f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Reverse)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = yPosition.dp)
                .background(brush = Brush.horizontalGradient(colors = listOf(Color.Transparent, TechPrimary, Color.Transparent)))
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = TechPrimary.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = size)
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
        Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(result.result, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text("Confidence: ${"%.1f".format(result.confidence)}%", style = MaterialTheme.typography.bodyMedium, color = TechTextSecondary)

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
fun RecommendationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, bgColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).clickable { }.padding(12.dp),
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
fun HistoryItemCard(item: ScanHistoryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.History, null, tint = TechTextSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val isRisk = item.result.contains("Depresi", ignoreCase = true)
                Text(item.result, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isRisk) Color.Red else Color(0xFF2E7D32))
                Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
            }
            Text("${item.confidence.toInt()}%", fontWeight = FontWeight.Bold, color = TechPrimary)
        }
    }
}