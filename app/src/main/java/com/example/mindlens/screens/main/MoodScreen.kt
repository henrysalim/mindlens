package com.example.mindlens.screens.main

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.helpers.formatDiaryDate
import com.example.mindlens.ui.HomeViewModel
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onBackClick: () -> Unit,
    onItemClick: (DiaryEntry) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    // State untuk Dialog Edit
    var showEditDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<DiaryEntry?>(null) }

    // Form State untuk Edit (Judul, Konten, Mood, Warna)
    var editTitle by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editMood by remember { mutableStateOf("") }
    var editColor by remember { mutableStateOf<Long>(0) }

    LaunchedEffect(Unit) {
        viewModel.loadEntries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Journey", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
        containerColor = TechBackground
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TechTextPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.entries.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                            Text("No entries found yet.", color = Color.Gray)
                        }
                    }
                } else {
                    items(state.entries) { entry ->
                        DiaryItemWithAction(
                            entry = entry,
                            onClick = { onItemClick(entry) },
                            onEdit = {
                                // Persiapkan data untuk diedit
                                entryToEdit = entry
                                editTitle = entry.title
                                editContent = entry.content
                                editMood = entry.mood
                                editColor = entry.color
                                showEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteDiaryEntry(entry.id)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // --- DIALOG EDIT LENGKAP ---
        if (showEditDialog && entryToEdit != null) {
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                containerColor = TechSurface,
                title = { Text("Edit Diary", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                text = {
                    Column {
                        // 1. Edit Judul
                        Text("Title", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Edit Mood (Pilih Icon Baru)
                        Text("Change Mood", fontSize = 14.sp, color = TechTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        EditMoodSelector(
                            currentMood = editMood,
                            onMoodSelected = { mood, color ->
                                editMood = mood
                                editColor = color.toArgb().toLong()
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3. Edit Konten
                        Text("Content", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Update data ke ViewModel
                            val updatedEntry = entryToEdit!!.copy(
                                title = editTitle,
                                content = editContent,
                                mood = editMood,     // Mood Baru
                                color = editColor    // Warna Baru
                            )
                            viewModel.updateDiaryEntry(updatedEntry)
                            showEditDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                    ) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// --- KOMPONEN PEMILIH MOOD DI DALAM DIALOG ---
@Composable
fun EditMoodSelector(
    currentMood: String,
    onMoodSelected: (String, Color) -> Unit
) {
    // Definisi Data Mood (Sama seperti di Home)
    val moodList = listOf(
        Triple("Awful", Color(0xFFE57373), Icons.Outlined.SentimentVeryDissatisfied),
        Triple("Bad", Color(0xFFFFB74D), Icons.Outlined.SentimentDissatisfied),
        Triple("Okay", Color(0xFFFFF176), Icons.Outlined.SentimentNeutral),
        Triple("Good", Color(0xFFAED581), Icons.Outlined.SentimentSatisfied),
        Triple("Great", Color(0xFF64B5F6), Icons.Outlined.SentimentVerySatisfied)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        moodList.forEach { (moodName, color, icon) ->
            val isSelected = currentMood.equals(moodName, ignoreCase = true)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        // Tambahkan border jika dipilih
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) TechPrimary else Color.Transparent,
                            shape = CircleShape
                        )
                        .background(color.copy(alpha = 0.3f))
                        .clickable { onMoodSelected(moodName, color) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = moodName,
                        tint = if (isSelected) color.copy(alpha = 1f) else color.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = moodName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isSelected) TechTextPrimary else TechTextSecondary
                )
            }
        }
    }
}

// --- KOMPONEN ITEM LIST ---
@Composable
fun DiaryItemWithAction(
    entry: DiaryEntry,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Garis Warna
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(entry.color))
            )
            Spacer(modifier = Modifier.width(16.dp))

            // Teks Judul & Tanggal
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TechTextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${formatDiaryDate(entry.createdAt)} • ${entry.mood}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechTextSecondary
                )
            }

            // Menu Dropdown (Titik Tiga)
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = TechTextSecondary
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = TechSurface
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            expanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}