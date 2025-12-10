package com.example.mindlens.screens.main

import android.os.Build
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.helpers.formatDiaryDate
// Pastikan import warna ini sesuai dengan file Theme Anda
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary
import com.example.mindlens.ui.TechSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    entry: DiaryEntry,
    onBackClick: () -> Unit
) {
    // Konversi Int color dari database kembali ke object Color Compose
    val moodColor = Color(entry.color)

    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Diary Detail",
                        color = TechTextPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
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
            // --- 1. Mood Header Banner ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Sedikit dipertinggi agar lebih proporsional
                    .background(
                        Brush.verticalGradient(
                            listOf(moodColor.copy(alpha = 0.7f), TechBackground)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Mood Icon Circle
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(moodColor.copy(alpha = 0.4f))
                            .padding(8.dp) // Outer ring effect
                            .background(moodColor.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfied,
                            contentDescription = "Mood Icon",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = entry.mood,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // --- 2. Details Content ---
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {

                // Judul Diary
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TechTextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Baris Tanggal (Menggunakan Helper baru)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(TechSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TechTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDiaryDate(entry.createdAt), // Memanggil fungsi helper baru
                        style = MaterialTheme.typography.labelMedium,
                        color = TechTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider Halus
                HorizontalDivider(
                    color = TechTextSecondary.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Isi Diary (Content)
                Card(
                    colors = CardDefaults.cardColors(containerColor = TechSurface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 250.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = entry.content,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                lineHeight = 28.sp // Line height lebih lega agar mudah dibaca
                            ),
                            color = TechTextPrimary
                        )
                    }
                }

                // Spacer bawah agar scroll tidak mentok
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}