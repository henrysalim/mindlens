package com.example.mindlens.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindlens.data.allExercises
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.home.ExerciseItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityType: String,
    onBack: () -> Unit,
    onVideoClick: (String, String, String) -> Unit
) {
    // Filter data based on the chosen activity
    val exercises = when(activityType) {
        "Yoga" -> allExercises.filter { it.id.startsWith("yoga") }
        "Meditation" -> allExercises.filter { it.id.startsWith("med") }
        else -> emptyList()
    }

    // Dynamic header image
    val heroImage = when(activityType) {
        "Yoga" -> "https://images.unsplash.com/photo-1599447421405-075710062669?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        "Meditation" -> "https://images.unsplash.com/photo-1528319725582-ddc096101511?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
        else -> "https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"
    }

    Scaffold(
        topBar = {
            // the top bar
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
            // header section
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

            // Exercises List
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