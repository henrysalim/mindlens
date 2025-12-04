package com.example.mindlens.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseGuideScreen(exerciseId: String, onBack: () -> Unit) {
    // Mengambil data dari list global di ActivityDetailScreen
    val exercise = allExercises.find { it.id == exerciseId } ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(0.6f))
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(TechBackground)
        ) {
            // 1. HEADER IMAGE
            item {
                Box {
                    AsyncImage(
                        model = exercise.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(320.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .align(Alignment.BottomCenter)
                            .background(Brush.verticalGradient(listOf(Color.Transparent, TechBackground)))
                    )
                }
            }

            // 2. INFO & INSTRUCTIONS
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(exercise.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = TechPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(exercise.duration, fontWeight = FontWeight.SemiBold, color = TechPrimary, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(exercise.description, style = MaterialTheme.typography.bodyLarge, color = TechTextSecondary)

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.LightGray.copy(0.5f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Step-by-Step Guide", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 3. STEPS LIST
            itemsIndexed(exercise.steps) { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(TechPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text((index + 1).toString(), fontWeight = FontWeight.Bold, color = TechPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechTextPrimary,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(50.dp)) }
        }
    }
}