package com.example.mindlens.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.viewModels.HomeViewModel
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary
import com.example.mindlens.ui.components.home.DiaryItemWithAction
import com.example.mindlens.ui.components.home.EditDiaryDialogue
import com.example.mindlens.ui.components.home.EditMoodSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onBackClick: () -> Unit,
    onItemClick: (DiaryEntry) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsState()

    // Edit dialogue state
    var showEditDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<DiaryEntry?>(null) }

    // Delete dialogue state
    var showDeleteDialog by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadEntries()
    }

    Scaffold(
        topBar = {
            // the top bar
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
                        // display the text if no diary entries found
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                            Text("No entries found yet.", color = Color.Gray)
                        }
                    }
                } else {
                    // display the diary entries
                    items(state.entries) { entry ->
                        DiaryItemWithAction(
                            entry = entry,
                            onClick = { onItemClick(entry) },
                            onEdit = {
                                entryToEdit = entry
                                showEditDialog = true
                            },
                            onDelete = {
                                entryToDelete = entry
                                showDeleteDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }
        }

        // Edit form
        if (showEditDialog && entryToEdit != null) {
            EditDiaryDialogue(
                entry = entryToEdit!!,
                onDismiss = {  },
                onConfirm = { updatedEntry ->
                    viewModel.updateDiaryEntry(updatedEntry)
                }
            )
        }

        // Delete confirmation
        if (showDeleteDialog && entryToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = TechSurface,
                title = {
                    Text("Hapus data?", fontWeight = FontWeight.Bold, color = TechTextPrimary)
                },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menghapus diary \"${entryToDelete?.title}\"? Aksi ini tidak dapat dibatalkan",
                        color = TechTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // delete diary
                            viewModel.deleteDiaryEntry(entryToDelete!!.id)
                            entryToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Batal", color = TechTextSecondary)
                    }
                }
            )
        }
    }
}