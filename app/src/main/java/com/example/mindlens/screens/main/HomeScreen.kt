package com.example.mindlens.screens.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // Import Baru
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle // Import Baru
import androidx.lifecycle.LifecycleEventObserver // Import Baru
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.helpers.formatDiaryDate
import com.example.mindlens.ui.*
import com.example.mindlens.ui.HomeUiEvent
import com.example.mindlens.ui.HomeViewModel
import com.example.mindlens.ui.WeeklyData
import com.example.mindlens.ui.components.CustomToast
import com.example.mindlens.viewModel.AuthViewModel
import com.google.android.gms.location.LocationServices

// --- DATA MODELS UI LOKAL ---
data class ActivityItem(
    val title: String,
    val duration: String,
    val icon: ImageVector,
    val color: Color
)

data class ArticleItem(
    val title: String,
    val category: String,
    val readTime: String
)

data class HomeScanItem(
    val result: String,
    val date: String,
    val confidence: Int,
    val isRisk: Boolean
)

val dummyHomeScans = listOf(
    HomeScanItem("Normal / Sehat", "Hari ini, 08:30", 92, false),
    HomeScanItem("Indikasi Stress", "Kemarin, 20:15", 74, true)
)

// --- MAIN COMPOSABLE ---
@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToActivity: (String) -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToDetail: (DiaryEntry) -> Unit = {},
    onNavigateToPanic: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocation = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val state by viewModel.uiState.collectAsState()
    val userName = remember { authViewModel.getUserName() }

    /// REQUEST LOCATION
    var onGrantedCallback by remember { mutableStateOf<((Location) -> Unit)?>(null) }
    var onDeniedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Pastikan permission memang granted sebelum akses lastLocation
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocation.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            onGrantedCallback?.invoke(loc)
                        } else {
                            // fallback request fresh location
                            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                1000L
                            ).setMaxUpdates(1).build()

                            val callback = object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    result.lastLocation?.let { onGrantedCallback?.invoke(it) }
                                    fusedLocation.removeLocationUpdates(this)
                                }
                            }

                            fusedLocation.requestLocationUpdates(locationRequest, callback, null)
                        }
                    }
                } else {
                    // Safety fallback kalau somehow permission ilang
                    onDeniedCallback?.invoke()
                }
            } else {
                onDeniedCallback?.invoke()
            }

            onGrantedCallback = null
            onDeniedCallback = null
        }

    fun onRequestSave(
        onPermissionGranted: (Location) -> Unit,
        onPermissionDenied: () -> Unit
    ) {
        onGrantedCallback = onPermissionGranted
        onDeniedCallback = onPermissionDenied
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // --- TAMBAHAN PENTING: REFRESH OTOMATIS SAAT BALIK KE HOME ---
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Setiap kali layar ini muncul (termasuk saat tombol Back ditekan),
                // kita paksa muat ulang data dari database
                viewModel.loadEntries()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // -----------------------------------------------------------


    // REQUEST LOCATION

    fun requestLocationOptional(
        fusedLocation: com.google.android.gms.location.FusedLocationProviderClient,
        context: Context,
        onLocation: (Location?) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    onLocation(loc)
                } else {
                    // fallback ambil lokasi baru
                    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        1000L
                    ).setMaxUpdates(1).build()

                    val callback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            onLocation(result.lastLocation)
                            fusedLocation.removeLocationUpdates(this)
                        }
                    }
                    fusedLocation.requestLocationUpdates(locationRequest, callback, null)
                }
            }
        } else {
            // permission belum granted, kembalikan null
            onLocation(null)
        }
    }


    // -----------------------------------------------------------

    // State Input Lokal
    var showDiaryDialog by remember { mutableStateOf(false) }
    var selectedMoodForEntry by remember { mutableStateOf("") }
    var selectedColorForEntry by remember { mutableStateOf(Color.Gray) }
    var diaryTitleText by remember { mutableStateOf("") }
    var diaryInputText by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is HomeUiEvent.SaveSuccess -> {
                    showDiaryDialog = false
                    showSuccessToast = true
                    diaryTitleText = ""
                    diaryInputText = ""
                    selectedMoodForEntry = ""
                    selectedColorForEntry = Color.Gray
                }
                is HomeUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(TechBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            HomeHeader(onPanicClick = onNavigateToPanic, username = userName)

            MoodCheckInSection(
                onMoodSelected = { mood, color ->
                    selectedMoodForEntry = mood
                    selectedColorForEntry = color
                    showDiaryDialog = true
                }
            )

            // CHART & TEXT INDIKASI
            WeeklyChartSection(
                weeklyData = state.weeklyStats,
                averageMoodStatus = state.averageMood // Menampilkan status mood terbaru
            )

            RecentScansHomeSection(
                scans = dummyHomeScans,
                onScanClick = onNavigateToScan
            )

            Spacer(modifier = Modifier.height(24.dp))

            DailyPhysioSection(
                onItemClick = { type ->
                    if (type == "Breathing") onNavigateToBreathing() else onNavigateToActivity(type)
                }
            )

            // LIST HISTORY TERBARU
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TechPrimary)
                }
            } else {
                RecentHistorySection(
                    diaryList = state.entries,
                    onSeeAllClick = onNavigateToHistory,
                    onItemClick = { entry -> onNavigateToDetail(entry) }
                )
            }
            RecommendedReadsSection()
        }

        // --- DIALOG INPUT DIARY ---
        if (showDiaryDialog) {
            AlertDialog(
                onDismissRequest = { showDiaryDialog = false },
                containerColor = TechSurface,
                title = {
                    Text("Feeling $selectedMoodForEntry", fontWeight = FontWeight.Bold, color = TechTextPrimary)
                },
                text = {
                    Column {
                        Text("Title", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = diaryTitleText,
                            onValueChange = { diaryTitleText = it },
                            placeholder = { Text("Short title...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechPrimary,
                                unfocusedBorderColor = TechTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("What made you feel this way?", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = diaryInputText,
                            onValueChange = { diaryInputText = it },
                            placeholder = { Text("Write here...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechPrimary,
                                unfocusedBorderColor = TechTextSecondary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                // ambil lokasi & save
                                requestLocationOptional(fusedLocation, context) { location ->
                                    viewModel.saveDiaryEntry(
                                        title = diaryTitleText,
                                        content = diaryInputText,
                                        mood = selectedMoodForEntry.ifEmpty { "Neutral" },
                                        colorInt = selectedColorForEntry.toArgb(),
                                        latitude = location?.latitude,
                                        longitude = location?.longitude
                                    )
                                }
                            } else {
                                // 🔥 SAVE TANPA LOKASI
                                viewModel.saveDiaryEntry(
                                    title = diaryTitleText,
                                    content = diaryInputText,
                                    mood = selectedMoodForEntry.ifEmpty { "Neutral" },
                                    colorInt = selectedColorForEntry.toArgb(),
                                    latitude = null,
                                    longitude = null
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiaryDialog = false }) { Text("Cancel") }
                }
            )
        }

        CustomToast(
            visible = showSuccessToast,
            message = "Diary saved & Analytics updated!",
            onDismiss = { showSuccessToast = false }
        )
    }
}

// ---------------- KOMPONEN UI (SECTIONS) ----------------

@Composable
fun HomeHeader(onPanicClick: () -> Unit, username: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(Brush.linearGradient(listOf(TechPrimary, Color(0xFF004E55))))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Hello, ${username}",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "MindLens is ready.",
                        color = Color.White.copy(0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Button(
                    onClick = onPanicClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Sos, contentDescription = "SOS", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun MoodCheckInSection(onMoodSelected: (String, Color) -> Unit) {
    val colorAwful = Color(0xFFE57373)
    val colorBad = Color(0xFFFFB74D)
    val colorNeutral = Color(0xFFFFF176)
    val colorGood = Color(0xFFAED581)
    val colorGreat = Color(0xFF64B5F6)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-50).dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = TechSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("How are you feeling?", fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MoodButton(Icons.Outlined.SentimentVeryDissatisfied, colorAwful, "Awful") { onMoodSelected("Awful", colorAwful) }
                MoodButton(Icons.Outlined.SentimentDissatisfied, colorBad, "Bad") { onMoodSelected("Bad", colorBad) }
                MoodButton(Icons.Outlined.SentimentNeutral, colorNeutral, "Okay") { onMoodSelected("Okay", colorNeutral) }
                MoodButton(Icons.Outlined.SentimentSatisfied, colorGood, "Good") { onMoodSelected("Good", colorGood) }
                MoodButton(Icons.Outlined.SentimentVerySatisfied, colorGreat, "Great") { onMoodSelected("Great", colorGreat) }
            }
        }
    }
}

@Composable
fun MoodButton(icon: ImageVector, color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)).clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = TechTextSecondary, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun WeeklyChartSection(
    weeklyData: List<WeeklyData>,
    averageMoodStatus: String
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-30).dp)) {
        Text("Weekly Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Rata-rata 7 Hari Terakhir:", style = MaterialTheme.typography.labelMedium, color = TechTextSecondary)
        Text(averageMoodStatus, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechPrimary)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = TechSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            if (weeklyData.all { it.score == 0f }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada aktivitas minggu ini", color = TechTextSecondary)
                }
            } else {
                Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                        val width = size.width
                        val height = size.height
                        val path = Path()
                        val stepX = width / (weeklyData.size - 1).coerceAtLeast(1)

                        weeklyData.forEachIndexed { index, data ->
                            val x = index * stepX
                            val y = height - (data.score * height * 0.8f)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            if (data.score > 0) {
                                drawCircle(color = data.color, radius = 12f, center = Offset(x, y))
                                drawCircle(color = Color.White, radius = 6f, center = Offset(x, y))
                            }
                        }
                        drawPath(path = path, color = TechPrimary.copy(alpha = 0.5f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weeklyData.forEach {
                            Text(it.day, fontSize = 10.sp, color = if (it.score > 0) TechTextPrimary else TechTextSecondary, fontWeight = if (it.score > 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyPhysioSection(onItemClick: (String) -> Unit) {
    val activities = listOf(
        ActivityItem("Meditation", "10 min", Icons.Rounded.SelfImprovement, Color(0xFFE1BEE7)),
        ActivityItem("Yoga", "15 min", Icons.Rounded.FitnessCenter, Color(0xFFB2DFDB)),
        ActivityItem("Breathing", "5 min", Icons.Rounded.Air, Color(0xFFBBDEFB))
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-16).dp)) {
        Text("Mind & Body", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activities) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(120.dp).clickable { onItemClick(item.title) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(item.icon, contentDescription = null, tint = TechTextPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Text(item.duration, fontSize = 12.sp, color = TechTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendedReadsSection() {
    val articles = listOf(
        ArticleItem("Understanding Anxiety", "Education", "5 min"),
        ArticleItem("The Power of Sleep", "Health", "3 min"),
        ArticleItem("Journaling 101", "Productivity", "7 min")
    )
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Recommended for You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        articles.forEach { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(TechPrimary.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Article, null, tint = TechPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(article.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Row { Text(article.category, fontSize = 10.sp, color = TechAccent); Text(" • ${article.readTime}", fontSize = 10.sp, color = TechTextSecondary) }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentHistorySection(diaryList: List<DiaryEntry>, onSeeAllClick: () -> Unit, onItemClick: (DiaryEntry) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Diaries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Text("See All", color = TechPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onSeeAllClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (diaryList.isEmpty()) {
            Text("No diaries yet. Add one above!", color = TechTextSecondary, fontSize = 12.sp)
        } else {
            diaryList.take(3).forEach { entry ->
                DiaryItem(entry, onClick = { onItemClick(entry) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DiaryItem(entry: DiaryEntry, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp)).background(Color(entry.color)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TechTextPrimary, maxLines = 1)
                Text("${formatDiaryDate(entry.createdAt)} • ${entry.mood}", style = MaterialTheme.typography.labelSmall, color = TechTextSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TechTextSecondary)
        }
    }
}

@Composable
fun RecentScansHomeSection(scans: List<HomeScanItem>, onScanClick: () -> Unit = {}) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Health Scans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Text("Scan Now", color = TechPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onScanClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        scans.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onScanClick() }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (item.isRisk) Icons.Default.Warning else Icons.Default.CheckCircle, null, tint = if (item.isRisk) Color(0xFFEF5350) else Color(0xFF66BB6A), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.result, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
                    }
                    Text("${item.confidence}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TechPrimary)
                }
            }
        }
    }
}