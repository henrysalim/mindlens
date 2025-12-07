package com.example.mindlens.screens.main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.ui.TechBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.helpers.formatDiaryDate
import com.example.mindlens.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DiaryDetailScreen(
    entry: DiaryEntry,
    onBackClick: () -> Unit
) {
    val moodColor = Color(entry.color)

    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                title = { Text("Diary Detail", color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TechTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 1. Mood Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(moodColor.copy(alpha = 0.8f), TechBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Mood Icon/Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(moodColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfied,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // 2. Details Content
            Column(modifier = Modifier.padding(24.dp)) {
                // Title
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TechTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDiaryDate(entry.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = TechTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                HorizontalDivider(color = TechTextSecondary.copy(alpha = 0.2f))

                Spacer(modifier = Modifier.height(24.dp))

                // The Actual Diary Content
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 200.dp)
                ) {
                    Text(
                        text = entry.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechTextPrimary,
                        modifier = Modifier.padding(20.dp),
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}