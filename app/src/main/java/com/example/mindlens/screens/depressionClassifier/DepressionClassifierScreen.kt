package com.example.mindlens.screens.depressionClassifier

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary
import com.example.mindlens.ui.components.depressionClassification.HistoryItemCard
import com.example.mindlens.ui.components.depressionClassification.ResultDashboard
import com.example.mindlens.ui.components.depressionClassification.ScanningEffect
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.viewModels.DepressionClassifierViewModel

@Composable
fun DepressionClassifierScreen(
    viewModel: DepressionClassifierViewModel = viewModel() // Use standard viewModel factory
) {
    val context = LocalContext.current

    // Initialize classifier once
    LaunchedEffect(Unit) {
        viewModel.initClassifier(context)
        viewModel.loadHistory()
    }

    // Observe ViewModel States
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val capturedBitmap by viewModel.capturedBitmap.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisResult by viewModel.analysisResult.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()
    val isHistoryLoading by viewModel.isHistoryLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val isToastVisible by viewModel.isToastVisible.collectAsState()

    // Local UI states for dialogs (these are strictly UI concerns)
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }
    var toastVisible by remember { mutableStateOf(false) }

    // Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.onImageSelected(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { viewModel.onImageCaptured(it) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
        else Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
    }

    // Confirm dialogs
    if (pendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            containerColor = TechSurface,
            title = { Text("Clear this Scan?", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
            text = { Text("This scan will be deleted. This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteScan(pendingDeleteId!!)
                        pendingDeleteId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("Cancel", color = TechTextSecondary) }
            }
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            containerColor = TechSurface,
            title = { Text("Clear All?", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
            text = { Text("All recent scans will be deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllScans()
                        showDeleteAllConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                ) { Text("Delete All") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancel", color = TechTextSecondary) }
            }
        )
    }

    // UI LAYOUT
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(TechBackground)
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AI Diagnostic",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                    Text(
                        "Analyze facial expressions using Deep Learning.",
                        color = TechTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.border(1.dp, Color(0xFF4CAF50), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "On-Device",
                            fontSize = 10.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Preview Section
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .border(
                        2.dp,
                        if (isAnalyzing) TechPrimary else Color.LightGray.copy(0.3f),
                        RoundedCornerShape(24.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    capturedBitmap != null -> {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Face, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Image Selected", color = Color.LightGray)
                        }
                    }
                }
                if (isAnalyzing) ScanningEffect()
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Buttons Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.Image, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedButton(
                    onClick = {
                        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            cameraLauncher.launch(null)
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(50.dp),
                    enabled = !isAnalyzing
                ) {
                    Icon(Icons.Outlined.PhotoCamera, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.analyzeImage(context) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White),
                enabled = (selectedImageUri != null || capturedBitmap != null) && !isAnalyzing
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Analyzing Face...")
                } else {
                    Icon(Icons.Default.Analytics, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Diagnosis")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Result Section
        item {
            AnimatedVisibility(
                visible = analysisResult != null,
                enter = slideInVertically() + fadeIn()
            ) {
                analysisResult?.let { ResultDashboard(it) }
            }
        }

        // History Section
        item {
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Recent Scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = { showDeleteAllConfirm = true },
                    enabled = scanHistory.isNotEmpty() && !isHistoryLoading
                ) {
                    Text("Clear All", color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isHistoryLoading && scanHistory.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(4.dp),
                    color = TechPrimary,
                    strokeWidth = 2.dp
                )
            }

            if (!isHistoryLoading && scanHistory.isEmpty()) {
                Text("No scan history yet.", color = TechTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // History List
        items(scanHistory, key = { it.id }) { history ->
            HistoryItemCard(
                item = history,
                onDeleteClick = { id -> pendingDeleteId = id }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (isToastVisible) {
        CustomToast(
            visible = isToastVisible,
            message = toastMessage ?: "",
            onDismiss = { viewModel._isToastVisible.value = false }
        )
    }
}