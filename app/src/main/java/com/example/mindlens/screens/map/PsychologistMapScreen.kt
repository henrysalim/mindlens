package com.example.mindlens.screens.map

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.mindlens.viewModels.HomeViewModel
import com.example.mindlens.ui.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.ActivityCompat
import com.example.mindlens.R
import android.provider.Settings
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.ui.components.map.StatItem

val jakartaCenter = LatLng(-6.175392, 106.827153)

// get user's current location
private suspend fun getAccurateLocation(
    context: Context,
    fused: FusedLocationProviderClient
): LatLng? {
    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) return null

    return try {
        // getCurrentLocation for a fresh location
        val loc = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        loc?.let { LatLng(it.latitude, it.longitude) }
    } catch (e: SecurityException) {
        Log.e("Location", "SecurityException in getAccurateLocation: ${e.message}", e)
        null
    } catch (e: Exception) {
        Log.e("Location", "Error getAccurateLocation: ${e.message}", e)
        null
    }
}
@Composable
fun PsychologistMapScreen(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    var editingDiaryId by remember { mutableStateOf<String?>(null) }
    var editingPosition by remember { mutableStateOf<LatLng?>(null) }
    var isLocationPermanentlyDenied by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var floatingOffset by remember { mutableStateOf<Offset?>(null) }
    var selectedDiaryId by remember { mutableStateOf<String?>(null) }
    var tooltipLatLng by remember { mutableStateOf<LatLng?>(null) }
    val scope = rememberCoroutineScope()
    val selectedDiary = remember(state.entries, selectedDiaryId) {
        state.entries.find { it.id == selectedDiaryId }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                userLocation = getAccurateLocation(context, fusedLocationClient)
            }
            isLocationPermanentlyDenied = false
        } else {
            val activity = context as? Activity
            val shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            } ?: false

            isLocationPermanentlyDenied = !shouldShowRationale
        }
    }

    // camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakartaCenter, 13f)
    }

    // request permission if needed, else fetch
    LaunchedEffect(key1 = true) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            userLocation = getAccurateLocation(context, fusedLocationClient)
        } else {
            permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // animate camera when userLocation available
    LaunchedEffect(userLocation) {
        userLocation?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    // Filter Data for last 7 days
    val recentDiaries = remember(state.entries) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        state.entries.filter { entry ->
            (entry.latitude != null && entry.latitude != 0.0) &&
                    (entry.longitude != null && entry.longitude != 0.0)
        }
    }

    // Calculate statistics for 5 categories
    val moodCounts = remember(recentDiaries) {
        mapOf(
            "Great" to recentDiaries.count { it.mood.lowercase() in listOf("great", "amazing", "bahagia") },
            "Good" to recentDiaries.count { it.mood.lowercase() in listOf("good", "senang") },
            "Neutral" to recentDiaries.count { it.mood.lowercase() in listOf("okay", "neutral", "biasa") },
            "Bad" to recentDiaries.count { it.mood.lowercase() in listOf("bad", "buruk") },
            "Awful" to recentDiaries.count { it.mood.lowercase() in listOf("awful", "terrible", "sedih") }
        )
    }

    // UI
    LaunchedEffect(editingDiaryId) {
        if (editingDiaryId != null) {
            Toast.makeText(
                context,
                "Drag the pin to update this emotional location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // TOOLTIP
    LaunchedEffect(
        tooltipLatLng,
        cameraPositionState.position
    ) {
        val latLng = tooltipLatLng ?: return@LaunchedEffect
        val projection = cameraPositionState.projection ?: return@LaunchedEffect

        val point = projection.toScreenLocation(latLng)
        floatingOffset = Offset(point.x.toFloat(), point.y.toFloat())
    }

    if (floatingOffset != null && selectedDiary != null) {
        val offset = floatingOffset!!
        val diary = selectedDiary!!
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        offset.x.toInt() - 200, // center horizontally
                        offset.y.toInt() - 350  // above marker
                    )
                }
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp)
                )
                .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                .padding(12.dp)
                .zIndex(10f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    diary.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        floatingOffset = null
                        editingDiaryId = selectedDiary!!.id
                        editingPosition = LatLng(
                            selectedDiary!!.latitude!!,
                            selectedDiary!!.longitude!!
                        )
                    }
                ) {
                    Text("Edit Location")
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // MapProperties: only enable my-location if we have permission
        val mapProperties = MapProperties(
            isMyLocationEnabled = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
        val uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)

        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapClick = {
                selectedDiaryId = null
                floatingOffset = null
            }
        ) {
            recentDiaries.forEach { diary ->
                if (diary.latitude != null && diary.longitude != null) {
                    val position = LatLng(diary.latitude, diary.longitude)
                    val (iconRes, color) = ImageUtils.getMoodVectorIcon(diary.mood)

                    val markerPosition =
                        if (editingDiaryId == diary.id && editingPosition != null)
                            editingPosition!!
                        else
                            position

                    key(diary.id) {
                        val markerState = rememberMarkerState(
                            position = markerPosition
                        )

                        // Observe Marker Movement
                        LaunchedEffect(editingDiaryId, markerState) {
                            if (editingDiaryId == diary.id) {
                                snapshotFlow { markerState.position }
                                    .collect { newPosition ->
                                        editingPosition = newPosition
                                    }
                            }
                        }

                        Marker(
                            state = markerState,
                            draggable = editingDiaryId == diary.id,
                            title = diary.mood,
                            snippet = diary.title,
                            icon = ImageUtils.bitmapDescriptorFromVector(context, iconRes, color),
                            onClick = {
                                selectedDiaryId = diary.id
                                tooltipLatLng = markerState.position
                                true
                            }
                        )
                    }
                }
            }

            // user location marker if available
            userLocation?.let { loc ->
                Marker(
                    state = MarkerState(position = loc),
                    title = "Your Location",
                    icon = BitmapDescriptorFactory.defaultMarker(180f)
                )
            }
        }
        if (editingDiaryId != null) {
            FloatingActionButton(
                onClick = {
                    val savedId = editingDiaryId!!

                    viewModel.updateDiaryLocation(
                        diaryId = savedId,
                        lat = editingPosition!!.latitude,
                        lng = editingPosition!!.longitude
                    )

                    editingDiaryId = null
                    editingPosition = null
                    selectedDiaryId = null
                    tooltipLatLng = null
                    floatingOffset = null
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                containerColor = TechPrimary
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save Location")
            }
        }

        // Statistics card above the map
        Card(
            modifier = Modifier
                .padding(top = 50.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Emotional Journey Map", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TechTextPrimary)
                Text("Your emotional journey over the last 7 days", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Row to fit 5 items in
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem("Great", moodCounts["Great"] ?: 0)
                    StatItem("Good", moodCounts["Good"] ?: 0)
                    StatItem("Neutral", moodCounts["Neutral"] ?: 0)
                    StatItem("Bad", moodCounts["Bad"] ?: 0)
                    StatItem("Awful", moodCounts["Awful"] ?: 0)
                }
            }
        }

        FloatingActionButton(
            onClick = {
                userLocation?.let {
                    cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 15f))
                } ?: run {
                    Toast.makeText(context, "Location not ready. Try again or enable GPS.", Toast.LENGTH_SHORT).show()
                }
            },
            containerColor = Color.White,
            contentColor = TechPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 110.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }

        if (
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Button(
                onClick = {
                    if (isLocationPermanentlyDenied) {
                        // open settings to enable location
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    } else {
                        // normal request permission
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Icon(Icons.Default.LocationOn, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isLocationPermanentlyDenied)
                        "Enable Location in Settings"
                    else
                        "Enable Location"
                )
            }
        }
    }
}