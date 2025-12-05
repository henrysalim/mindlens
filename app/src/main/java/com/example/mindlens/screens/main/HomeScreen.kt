package com.example.mindlens.screens.main

import android.os.Build
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.ui.* // Pastikan package theme benar
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.helpers.formatDiaryDate
import com.example.mindlens.ui.components.CustomToast
import com.example.mindlens.viewModel.AuthViewModel

// --- DATA MODELS ---
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

data class WeeklyData(
    val day: String,
    val score: Float,
    val color: Color
)

// --- MAIN COMPOSABLE ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    // Parameter Callback Navigasi (PENTING)
    onNavigateToHistory: () -> Unit = {},
    onNavigateToActivity: (String) -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToDetail: (DiaryEntry) -> Unit = {},
    onNavigateToPanic: () -> Unit = {},
    // Inject ViewModel (Opsional jika belum siap, pakai default)
    viewModel: HomeViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current

    val diaryList by viewModel.diaryHistory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // State untuk Dialog Diary (UI State)
    var showDiaryDialog by remember { mutableStateOf(false) }
    var selectedMoodForEntry by remember { mutableStateOf("") }
    var selectedColorForEntry by remember { mutableStateOf(Color.Gray) }
    var diaryInputText by remember { mutableStateOf("") }
    val userName = remember { authViewModel.getUserName() }

//    val diaryHistory = viewModel.diary

    // --- Toast State ---
    var showSuccessToast by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(TechBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Padding bawah agar tidak tertutup navbar
        ) {
            // 1. Header Section (Gradient + SOS)
            HomeHeader(onPanicClick = onNavigateToPanic, username = userName)

            // 2. Mood Check-in Section
            MoodCheckInSection(
                onMoodSelected = { mood, color ->
                    selectedMoodForEntry = mood
                    showDiaryDialog = true
                    selectedColorForEntry = color
                }
            )

            // 3. Weekly Chart Section
            WeeklyChartSection()

            // 4. Daily Activities Section (Yoga, Breathing, Meditation)
            DailyPhysioSection(
                onItemClick = { type ->
                    if (type == "Breathing") {
                        onNavigateToBreathing() // Panggil callback Breathing
                    } else {
                        onNavigateToActivity(type) // Panggil callback Activity List
                    }
                }
            )

            // 5. Recent History Section (Diary List)
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (diaryList.isEmpty()) {
                // Empty State
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No memories yet. Write one!", color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                RecentHistorySection(
                    diaryList = diaryList,
                    onSeeAllClick = onNavigateToHistory, // Panggil callback History
                    onItemClick = { entry ->
                        onNavigateToDetail(entry)
                    }
                )
            }

            // 6. Recommended Reads Section
            RecommendedReadsSection()
        }

        // --- DIALOG CRUD DIARY ---
        if (showDiaryDialog) {
            AlertDialog(
                onDismissRequest = { showDiaryDialog = false },
                containerColor = TechSurface,
                title = {
                    Text(
                        "Feeling $selectedMoodForEntry",
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                },
                text = {
                    Column {
                        Text("What made you feel this way?", fontSize = 14.sp, color = TechTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = diaryInputText,
                            onValueChange = { diaryInputText = it },
                            placeholder = { Text("Write here...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechPrimary,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (diaryInputText.isNotEmpty()) {
                                // Panggil fungsi ViewModel untuk menyimpan data
                                viewModel.saveDiaryEntry(
                                    content = diaryInputText,
                                    mood = selectedMoodForEntry.ifEmpty { "Neutral" }, // Fallback mood
                                    onSuccess = {
                                        // 2. Close Dialog & Show Toast
                                        showDiaryDialog = false
                                        showSuccessToast = true

                                        // 3. Reset inputs
                                        diaryInputText = ""
                                        selectedMoodForEntry = ""
                                    },
                                    onError = { errorMsg ->
                                        Log.d("error submit", errorMsg)
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                        // Do NOT close dialog here so user can try again
                                    },
                                    colorInt = selectedColorForEntry.toArgb(),
                                )

                                // Reset UI
                                diaryInputText = ""
                                showDiaryDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiaryDialog = false }) {
                        Text("Cancel", color = TechTextSecondary)
                    }
                },
                // --- The Custom Toast Overlay ---
                // Placing it here ensures it draws ON TOP of everything
            )
        }

        CustomToast(
            visible = showSuccessToast,
            message = "Data successfully stored!",
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
                // TOMBOL SOS / DARURAT
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset(y = (-50).dp), // Efek melayang di atas header
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
                MoodButton(Icons.Outlined.SentimentVeryDissatisfied, MoodTerrible, "Awful") {
                    onMoodSelected(
                        "Awful",
                        MoodTerrible
                    )
                }
                MoodButton(Icons.Outlined.SentimentDissatisfied, MoodBad, "Bad") { onMoodSelected("Bad", MoodBad) }
                MoodButton(Icons.Outlined.SentimentNeutral, MoodNeutral, "Okay") { onMoodSelected("Okay", MoodNeutral) }
                MoodButton(Icons.Outlined.SentimentSatisfied, MoodGood, "Good") { onMoodSelected("Good", MoodGood) }
                MoodButton(Icons.Outlined.SentimentVerySatisfied, MoodGreat, "Great") {
                    onMoodSelected(
                        "Great",
                        MoodGreat
                    )
                }
            }
        }
    }
}

@Composable
fun MoodButton(icon: ImageVector, color: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = TechTextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun WeeklyChartSection() {
    val weeklyData = listOf(
        WeeklyData("Mon", 0.2f, MoodBad),
        WeeklyData("Tue", 0.5f, MoodNeutral),
        WeeklyData("Wed", 0.8f, MoodGreat),
        WeeklyData("Thu", 0.6f, MoodGood),
        WeeklyData("Fri", 0.9f, MoodGreat),
        WeeklyData("Sat", 0.4f, MoodNeutral),
        WeeklyData("Sun", 0.7f, MoodGood)
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-30).dp)) {
        Text(
            "Weekly Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TechTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = TechSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                    val width = size.width
                    val height = size.height
                    val path = Path()
                    val stepX = width / (weeklyData.size - 1)

                    weeklyData.forEachIndexed { index, data ->
                        val x = index * stepX
                        val y = height - (data.score * height)
                        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(color = data.color, radius = 12f, center = Offset(x, y))
                    }
                    drawPath(path = path, color = Color.LightGray, style = Stroke(width = 4f, cap = StrokeCap.Round))
                }
                Row(
                    modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weeklyData.forEach {
                        Text(
                            it.day,
                            fontSize = 10.sp,
                            color = TechTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text("Happy", fontSize = 8.sp, color = MoodGreat, modifier = Modifier.align(Alignment.TopStart))
                Text(
                    "Sad",
                    fontSize = 8.sp,
                    color = MoodTerrible,
                    modifier = Modifier.align(Alignment.BottomStart).padding(bottom = 20.dp)
                )
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
        Text(
            "Mind & Body",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TechTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activities) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .width(120.dp)
                        .clickable { onItemClick(item.title) } // Callback Klik Item
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = TechTextPrimary,
                            modifier = Modifier.size(28.dp)
                        )
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
        Text(
            "Recommended for You",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TechTextPrimary
        )
        Spacer(modifier = Modifier.height(12.dp))

        articles.forEach { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp))
                            .background(TechPrimary.copy(0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Article, null, tint = TechPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(article.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Row {
                            Text(article.category, fontSize = 10.sp, color = TechAccent)
                            Text(" • ${article.readTime}", fontSize = 10.sp, color = TechTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecentHistorySection(
    diaryList: List<DiaryEntry>,
    onSeeAllClick: () -> Unit,
    onItemClick: (DiaryEntry) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Diaries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TechTextPrimary
            )
            Text(
                "See All",
                color = TechPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSeeAllClick() } // Callback Klik See All
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (diaryList.isEmpty()) {
            Text("No diaries yet. Add one above!", color = TechTextSecondary, fontSize = 12.sp)
        } else {
            diaryList.take(3).forEach { entry ->
                DiaryItem(entry, onClick = { onItemClick(entry) }) // Callback Klik Diary Item
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryItem(entry: DiaryEntry, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.clickable { onClick() } // Enable Click
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).clip(RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TechTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${formatDiaryDate(entry.createdAt)} • ${entry.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechTextSecondary
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = TechTextSecondary)
        }
    }
}