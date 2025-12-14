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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.data.HomeScanItem
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.ui.components.home.DailyPhysioSection
import com.example.mindlens.ui.components.home.HomeHeader
import com.example.mindlens.ui.components.home.MoodCheckInSection
import com.example.mindlens.ui.components.home.RecentHistorySection
import com.example.mindlens.ui.components.home.RecentScansHomeSection
import com.example.mindlens.ui.components.home.WeeklyChartSection
import com.example.mindlens.viewModels.AuthViewModel
import com.example.mindlens.viewModels.HomeUiEvent
import com.example.mindlens.viewModels.HomeViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// TODO: perlu diganti dengan data riil
val dummyHomeScans = listOf(
    HomeScanItem("Normal / Sehat", "Hari ini, 08:30", 92, false),
    HomeScanItem("Indikasi Stress", "Kemarin, 20:15", 74, true)
)

// Main home screen composable
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

    // Local input state
    var showDiaryDialog by remember { mutableStateOf(false) }
    var selectedMoodForEntry by remember { mutableStateOf("") }
    var selectedColorForEntry by remember { mutableStateOf(Color.Gray) }
    var diaryTitleText by remember { mutableStateOf("") }
    var diaryInputText by remember { mutableStateOf("") }
    var showSuccessToast by remember { mutableStateOf(false) }

    // Auto refresh
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // reload data from database every time this page opened (better use caching if possible...but don't have any time -> for next developer 🔥)
                viewModel.loadEntries()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Request location
    fun requestLocationOptional(
        fusedLocation: FusedLocationProviderClient,
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
                    val locationRequest = LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        1000L
                    ).setMaxUpdates(1).build()

                    val callback = object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            onLocation(result.lastLocation)
                            fusedLocation.removeLocationUpdates(this)
                        }
                    }
                    fusedLocation.requestLocationUpdates(locationRequest, callback, null)
                }
            }
        } else {
            // return null if permission not granted
            onLocation(null)
        }
    }

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
            // header
            HomeHeader(onPanicClick = onNavigateToPanic, username = userName)

            // to submit diary
            MoodCheckInSection(
                onMoodSelected = { mood, color ->
                    selectedMoodForEntry = mood
                    selectedColorForEntry = color
                    showDiaryDialog = true
                }
            )

            // Mood Chart
            WeeklyChartSection(
                weeklyData = state.weeklyStats,
                averageMoodStatus = state.averageMood
            )

            // displaying recent scan
            RecentScansHomeSection(
                scans = dummyHomeScans,
                onScanClick = onNavigateToScan
            )

            Spacer(modifier = Modifier.height(24.dp))

            // exercises
            DailyPhysioSection(
                onItemClick = { type ->
                    if (type == "Breathing") onNavigateToBreathing() else onNavigateToActivity(type)
                }
            )

            // Recent diary
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
        }

        // Input diary dialogue
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
                                // get location & save
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
                                viewModel.saveDiaryEntry(
                                    title = diaryTitleText,
                                    content = diaryInputText,
                                    mood = selectedMoodForEntry.ifEmpty { "Neutral" },
                                    colorInt = selectedColorForEntry.toArgb(),
                                    latitude = null,
                                    longitude = null
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDiaryDialog = false }) { Text("Cancel") }
                }
            )
        }

        // toast to display messages
        CustomToast(
            visible = showSuccessToast,
            message = "Diary saved & Analytics updated!",
            onDismiss = { showSuccessToast = false }
        )
    }
}