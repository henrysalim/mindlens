package com.example.mindlens.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.mindlens.ui.* // Ensure this import matches your package structure

// --- DATA MODEL ---
data class ExerciseMock(
    val id: String,
    val title: String,
    val duration: String,
    val videoId: String,      // ID YouTube
    val imageUrl: String,
    val description: String,
    val steps: List<String>
)

// Helper Thumbnail YouTube
fun getYoutubeThumbnail(videoId: String) = "https://img.youtube.com/vi/$videoId/mqdefault.jpg"

// --- DATA DUMMY (Fixed Image URLs) ---
val allExercises = listOf(
    ExerciseMock(
        id = "yoga_1",
        title = "Morning Sun Salutation",
        duration = "10 min",
        videoId = "ZP34IA0d8LI",
        // Direct image link from Unsplash
        imageUrl = "https://images.unsplash.com/photo-1544367563-12123d896889?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Awaken your body with this flow.",
        steps = listOf("Step 1: Stand tall", "Step 2: Inhale arms up", "Step 3: Fold forward")
    ),
    ExerciseMock(
        id = "yoga_2",
        title = "Yoga for Stress Relief",
        duration = "15 min",
        videoId = "hJbRpHZr_d0",
        imageUrl = "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Hilangkan stres dan ketegangan otot.",
        steps = listOf("Step 1: Child's pose", "Step 2: Cat-Cow stretch")
    ),
    ExerciseMock(
        id = "med_1",
        title = "5 Min Meditation",
        duration = "5 min",
        videoId = "HNab2YqCCiM",
        imageUrl = "https://images.unsplash.com/photo-1593811167562-9cef47bfc4d7?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Meditasi cepat untuk fokus kembali.",
        steps = listOf("Sit comfortably", "Focus on breath")
    ),
    ExerciseMock(
        id = "med_2",
        title = "Deep Sleep Music",
        duration = "20 min",
        videoId = "IVDuU3anYCI",
        imageUrl = "https://images.unsplash.com/photo-1515023115689-589c33041697?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80",
        description = "Musik relaksasi untuk tidur nyenyak.",
        steps = listOf("Lie down", "Close eyes", "Relax muscles")
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityType: String,
    onBack: () -> Unit,
    onVideoClick: (String, String, String) -> Unit
) {
    val exercises = when(activityType) {
        "Yoga" -> allExercises.filter { it.id.startsWith("yoga") }
        "Meditation" -> allExercises.filter { it.id.startsWith("med") }
        else -> emptyList()
    }

    // Fixed Hero Images
    val heroImage = when(activityType) {
        "Yoga" -> "https://images.unsplash.com/photo-1599447421405-075710062669?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        "Meditation" -> "https://images.unsplash.com/photo-1528319725582-ddc096101511?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        else -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$activityType Session", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TechBackground)
                .padding(padding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
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
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                            Text("Start your journey", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("${exercises.size} sessions available", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Select Session", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
            }

            items(exercises) { exercise ->
                ExerciseItemCard(
                    exercise = exercise,
                    onClick = {
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(100.dp, 70.dp)) {
                AsyncImage(
                    model = getYoutubeThumbnail(exercise.videoId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = exercise.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary, maxLines = 2)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = TechPrimary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = exercise.duration, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TechPrimary)
                }
            }
        }
    }
}