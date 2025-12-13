package com.example.mindlens.screens.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // Import Baru
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle // Import Baru
import androidx.lifecycle.LifecycleEventObserver // Import Baru
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.ui.*
import com.example.mindlens.viewModels.HomeUiEvent
import com.example.mindlens.viewModels.HomeViewModel
import com.example.mindlens.ui.components.CustomToast
import com.example.mindlens.ui.components.home.DailyPhysioSection
import com.example.mindlens.ui.components.home.HomeHeader
import com.example.mindlens.ui.components.home.MoodCheckInSection
import com.example.mindlens.ui.components.home.RecentHistorySection
import com.example.mindlens.ui.components.home.RecentScansHomeSection
import com.example.mindlens.ui.components.home.RecommendedReadsSection
import com.example.mindlens.ui.components.home.WeeklyChartSection
import com.example.mindlens.viewModels.AuthViewModel
import com.google.android.gms.location.LocationServices

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

data class HomeScanItem(
    val result: String,
    val date: String,
    val confidence: Int,
    val isRisk: Boolean
)

val dummyHomeScans = listOf(
    HomeScanItem("Normal / Sehat", "Hari ini, 08:30", 92, false),
    HomeScanItem("Indikasi Stress", "Kemarin, 20:15", 74, true)
)

@Composable
fun HomeScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToActivity: (String) -> Unit = {},
    onNavigateToBreathing: () -> Unit = {},
    onNavigateToDetail: (DiaryEntry) -> Unit = {},
    onNavigateToPanic: () -> Unit = {},
    onNavigateToScan: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val fusedLocation = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val state by viewModel.uiState.collectAsState()
    val userName = remember { authViewModel.getUserName() }

    /// Request user's location
    var onGrantedCallback by remember { mutableStateOf<((Location) -> Unit)?>(null) }
    var onDeniedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                // Pastikan permission memang granted sebelum akses lastLocation
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocation.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            onGrantedCallback?.invoke(loc)
                        } else {
                            // fallback request fresh location
                            val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                                1000L
                            ).setMaxUpdates(1).build()

                            val callback = object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    result.lastLocation?.let { onGrantedCallback?.invoke(it) }
                                    fusedLocation.removeLocationUpdates(this)
                                }
                            }

                            fusedLocation.requestLocationUpdates(locationRequest, callback, null)
                        }
                    }
                } else {
                    // Safety fallback kalau somehow permission ilang
                    onDeniedCallback?.invoke()
                }
            } else {
                onDeniedCallback?.invoke()
            }

            onGrantedCallback = null
            onDeniedCallback = null
        }

    // Refresh home's data automatically
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Setiap kali layar ini muncul (termasuk saat tombol Back ditekan),
                // kita paksa muat ulang data dari database
                viewModel.loadEntries()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    // Request user's location
    fun requestLocationOptional(
        fusedLocation: com.google.android.gms.location.FusedLocationProviderClient,
        context: Context,
        onLocation: (Location?) -> Unit
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocation.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) {
                    onLocation(loc)
                } else {
                    // fallback ambil lokasi baru
                    val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        1000L
                    ).setMaxUpdates(1).build()

                    val callback = object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            onLocation(result.lastLocation)
                            fusedLocation.removeLocationUpdates(this)
                        }
                    }
                    fusedLocation.requestLocationUpdates(locationRequest, callback, null)
                }
            }
        } else {
            onLocation(null)
        }
    }


    // Input states
    var showDiaryDialog by remember { mutableStateOf(false) }
    var selectedMoodForEntry by remember { mutableStateOf("") }
    var selectedColorForEntry by remember { mutableStateOf(Color.Gray) }
    var diaryTitleText by remember { mutableStateOf("") }
    var diaryInputText by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when(event) {
                is HomeUiEvent.SaveSuccess -> {
                    showDiaryDialog = false
                    showSuccessToast = true
                    diaryTitleText = ""
                    diaryInputText = ""
                    selectedMoodForEntry = ""
                    selectedColorForEntry = Color.Gray
                }
                is HomeUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(TechBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp)
        ) {
            HomeHeader(onPanicClick = onNavigateToPanic, username = userName)

            MoodCheckInSection(
                onMoodSelected = { mood, color ->
                    selectedMoodForEntry = mood
                    selectedColorForEntry = color
                    showDiaryDialog = true
                }
            )

            // CHART & TEXT INDIKASI
            WeeklyChartSection(
                weeklyData = state.weeklyStats,
                averageMoodStatus = state.averageMood // Menampilkan status mood terbaru
            )

            RecentScansHomeSection(
                scans = dummyHomeScans,
                onScanClick = onNavigateToScan
            )

            Spacer(modifier = Modifier.height(24.dp))

            DailyPhysioSection(
                onItemClick = { type ->
                    if (type == "Breathing") onNavigateToBreathing() else onNavigateToActivity(type)
                }
            )

            // LIST HISTORY TERBARU
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TechPrimary)
                }
            } else {
                RecentHistorySection(
                    diaryList = state.entries,
                    onSeeAllClick = onNavigateToHistory,
                    onItemClick = { entry -> onNavigateToDetail(entry) }
                )
            }
            RecommendedReadsSection()
        }

        // --- DIALOG INPUT DIARY ---
        if (showDiaryDialog) {
            AlertDialog(
                onDismissRequest = { showDiaryDialog = false },
                containerColor = TechSurface,
                title = {
                    Text("Feeling $selectedMoodForEntry", fontWeight = FontWeight.Bold, color = TechTextPrimary)
                },
                text = {
                    Column {
                        Text("Title", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = diaryTitleText,
                            onValueChange = { diaryTitleText = it },
                            placeholder = { Text("Short title...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 1,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechPrimary,
                                unfocusedBorderColor = TechTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("What made you feel this way?", fontSize = 14.sp, color = TechTextSecondary)
                        OutlinedTextField(
                            value = diaryInputText,
                            onValueChange = { diaryInputText = it },
                            placeholder = { Text("Write here...") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TechPrimary,
                                unfocusedBorderColor = TechTextSecondary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                // ambil lokasi & save
                                requestLocationOptional(fusedLocation, context) { location ->
                                    viewModel.saveDiaryEntry(
                                        title = diaryTitleText,
                                        content = diaryInputText,
                                        mood = selectedMoodForEntry.ifEmpty { "Neutral" },
                                        colorInt = selectedColorForEntry.toArgb(),
                                        latitude = location?.latitude,
                                        longitude = location?.longitude
                                    )
                                }
                            } else {
                                // request permission dulu
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiaryDialog = false }) { Text("Cancel") }
                }
            )
        }

        CustomToast(
            visible = showSuccessToast,
            message = "Diary saved & Analytics updated!",
            onDismiss = { showSuccessToast = false }
        )
    }
}