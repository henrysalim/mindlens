package com.example.mindlens.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.ui.* // Pastikan ini mengimpor warna dari Theme.kt Anda

// --- DATA MODEL ---
data class ExerciseMock(
    val id: String,
    val title: String,
    val duration: String,
    val videoId: String,
    val imageUrl: String,
    val description: String,
    val steps: List<String>
)

// Helper Thumbnail YouTube
fun getYoutubeThumbnail(videoId: String) = "https://img.youtube.com/vi/$videoId/mqdefault.jpg"

// --- DATA DUMMY ---
val allExercises = listOf(
    // YOGA
    ExerciseMock(
        id = "yoga_1",
        title = "Morning Sun Salutation",
        duration = "10 min",
        videoId = "ZP34IA0d8LI", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1544367563-12123d896889?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Awaken your body with this flow to start your day with energy.",
        steps = listOf("Stand tall", "Inhale arms up", "Fold forward")
    ),
    ExerciseMock(
        id = "yoga_2",
        title = "Yoga for Stress Relief",
        duration = "15 min",
        videoId = "hJbRpHZr_d0", // Valid ID
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Hilangkan stres dan ketegangan otot setelah seharian bekerja.",
        steps = listOf("Child's pose", "Cat-Cow stretch", "Downward Dog")
    ),
    // MEDITATION
    ExerciseMock(
        id = "med_1",
        title = "5 Min Mindfulness",
        duration = "5 min",
        videoId = "HNab2YqCCiM", // Valid ID (diganti ke video mindfulness umum jika perlu)
        imageUrl = "https://images.unsplash.com/photo-1593811167562-9cef47bfc4d7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Meditasi cepat untuk mengembalikan fokus dan ketenangan.",
        steps = listOf("Sit comfortably", "Focus on breath", "Let go of thoughts")
    ),
    ExerciseMock(
        id = "med_2",
        title = "Deep Sleep Music",
        duration = "20 min",
        videoId = "IVDuU3anYCI", // Valid ID (Video Relaxing)
        imageUrl = "https://images.unsplash.com/photo-1515023115689-589c33041697?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Musik relaksasi dan panduan visual untuk tidur nyenyak.",
        steps = listOf("Lie down", "Close eyes", "Relax muscles")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityType: String, // "Yoga" atau "Meditation" (dikirim dari MainScreen)
    onBack: () -> Unit,
    onVideoClick: (String, String, String) -> Unit // (videoId, title, desc)
) {
    // Filter data berdasarkan tipe aktivitas yang dipilih
    val exercises = when(activityType) {
        "Yoga" -> allExercises.filter { it.id.startsWith("yoga") }
        "Meditation" -> allExercises.filter { it.id.startsWith("med") }
        else -> emptyList()
    }

    // Gambar Header Dinamis
    val heroImage = when(activityType) {
        "Yoga" -> "https://images.unsplash.com/photo-1599447421405-075710062669?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        "Meditation" -> "https://images.unsplash.com/photo-1528319725582-ddc096101511?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        else -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "$activityType Session",
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TechTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
        containerColor = TechBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TechBackground)
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp) // Padding bawah agar tidak ketutup navbar
        ) {
            // --- HEADER SECTION ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    Box {
                        AsyncImage(
                            model = heroImage,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Overlay Gelap agar teks terbaca
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                "Start your journey",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${exercises.size} sessions available",
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Select Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- LIST EXERCISES ---
            items(exercises) { exercise ->
                ExerciseItemCard(
                    exercise = exercise,
                    onClick = {
                        // Panggil Callback untuk navigasi ke VideoPlayerScreen
                        onVideoClick(exercise.videoId, exercise.title, exercise.description)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ExerciseItemCard(exercise: ExerciseMock, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail YouTube
            Box(modifier = Modifier.size(120.dp, 80.dp)) {
                AsyncImage(
                    model = getYoutubeThumbnail(exercise.videoId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
                // Overlay Play Icon
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TechTextPrimary,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = TechPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = exercise.duration,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TechPrimary
                    )
                }
            }
        }
    }
}