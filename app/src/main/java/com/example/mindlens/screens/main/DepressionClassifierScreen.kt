package com.example.mindlens.screens.main

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.ui.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- DATA MODEL ---
data class ScanHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: Uri?,
    val result: String,
    val confidence: Float,
    val date: String
)

@Composable
fun DepressionClassifierScreen() {
    val context = LocalContext.current

    // State untuk UI
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<ScanHistoryItem?>(null) }

    // State History (Simpan sementara di memory)
    val scanHistory = remember { mutableStateListOf<ScanHistoryItem>() }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            analysisResult = null // Reset hasil lama
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        // Note: Untuk produksi, simpan bitmap ke File/Uri. Di sini kita handle logic UI saja.
        // Anggap user ambil foto sukses.
    }

    // Logic Simulasi AI
    suspend fun analyzeImage() {
        isAnalyzing = true
        delay(3000) // Simulasi scanning 3 detik (biar kelihatan canggih)

        // DUMMY RESULT LOGIC (Randomizer)
        val isDepressed = (0..1).random() == 1
        val confidence = (75..98).random().toFloat()

        val resultItem = ScanHistoryItem(
            imageUri = selectedImageUri,
            result = if (isDepressed) "Indikasi Depresi" else "Normal / Sehat",
            confidence = confidence,
            date = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date())
        )

        analysisResult = resultItem
        scanHistory.add(0, resultItem) // Tambah ke history
        isAnalyzing = false
    }

    val scope = rememberCoroutineScope() // Untuk menjalankan suspend function

    // UI UTAMA
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBackground)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        // 1. Header Professional
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("AI Diagnostic", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                    Text("Analyze facial expressions using Deep Learning.", color = TechTextSecondary, fontSize = 12.sp)
                }
                // Privacy Badge (Fitur UX Canggih)
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

        // 2. Image Preview & Scanning Area
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
                if (selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // --- ANIMASI LASER SCANNING ---
                    if (isAnalyzing) {
                        ScanningEffect()
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Face, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Image Selected", color = Color.LightGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 3. Tombol Kontrol (Camera / Gallery / Analyze)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tombol Gallery
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

                // Tombol Camera (Placeholder Action)
                OutlinedButton(
                    onClick = { /* TODO: Implement Camera */ },
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

            // Tombol ANALYZE (Besar & Hijau)
            Button(
                onClick = {
                    // Trigger Analisis (menggunakan Coroutine di dalam LaunchedEffect atau Scope)
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                        analyzeImage()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                enabled = selectedImageUri != null && !isAnalyzing
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

        // 4. RESULT SECTION (Muncul setelah analisis)
        item {
            AnimatedVisibility(
                visible = analysisResult != null,
                enter = slideInVertically() + fadeIn()
            ) {
                analysisResult?.let { result ->
                    ResultDashboard(result)
                }
            }
        }

        // 5. HISTORY SECTION
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

// --- KOMPONEN CANGGIH ---

@Composable
fun ScanningEffect() {
    val infiniteTransition = rememberInfiniteTransition()
    val yPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f, // Sesuai tinggi box
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Garis Laser
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
        // Overlay Grid Hijau Tipis
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
    val isPositive = result.result.contains("Depresi")
    val color = if (isPositive) Color(0xFFEF5350) else Color(0xFF66BB6A)
    val icon = if (isPositive) Icons.Default.Warning else Icons.Default.CheckCircle

    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(result.result, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
                Text("Confidence Level: ${result.confidence}%", style = MaterialTheme.typography.bodyMedium, color = TechTextSecondary)

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.LightGray.copy(0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // REKOMENDASI DINAMIS
                Text("Recommended Action:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(8.dp))

                if (isPositive) {
                    RecommendationItem(Icons.Default.Call, "Call Helpline", "Talk to a professional now.", Color(0xFFFFF3E0))
                    Spacer(modifier = Modifier.height(8.dp))
                    RecommendationItem(Icons.Default.SelfImprovement, "Try Meditation", "Calm your mind with guided audio.", Color(0xFFE1F5FE))
                } else {
                    RecommendationItem(Icons.Default.SentimentSatisfied, "Keep it up!", "Maintain your good mood with journaling.", Color(0xFFE8F5E9))
                }
            }
        }
    }
}

@Composable
fun RecommendationItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String, bgColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { /* TODO */ }
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
fun HistoryItemCard(item: ScanHistoryItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.History, null, tint = TechTextSecondary)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.result, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (item.result.contains("Depresi")) Color.Red else Color(0xFF2E7D32))
                Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
            }
            Text("${item.confidence.toInt()}%", fontWeight = FontWeight.Bold, color = TechPrimary)
        }
    }
}