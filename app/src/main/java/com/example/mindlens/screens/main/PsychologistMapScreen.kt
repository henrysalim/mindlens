package com.example.mindlens.screens.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
import com.example.mindlens.ui.HomeViewModel
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Calendar

@Composable
fun PsychologistMapScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Lokasi Default: Tangerang
    val tangerang = LatLng(-6.1702, 106.6403)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(tangerang, 13f)
    }

    var hasPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            hasPermission = true
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        userLocation = currentLatLng
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLatLng, 15f)
                    }
                }
            } catch (e: SecurityException) {}
        }
    }

    // Filter Data 7 Hari Terakhir
    val recentDiaries = remember(state.entries) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        state.entries.filter { entry ->
            (entry.latitude != null && entry.latitude != 0.0) &&
                    (entry.longitude != null && entry.longitude != 0.0)
        }
    }

    // --- HITUNG STATISTIK 5 KATEGORI ---
    val moodCounts = remember(recentDiaries) {
        mapOf(
            "Great" to recentDiaries.count { it.mood.lowercase() in listOf("great", "amazing", "bahagia") },
            "Good" to recentDiaries.count { it.mood.lowercase() in listOf("good", "senang") },
            "Neutral" to recentDiaries.count { it.mood.lowercase() in listOf("okay", "neutral", "biasa") },
            "Bad" to recentDiaries.count { it.mood.lowercase() in listOf("bad", "buruk") },
            "Awful" to recentDiaries.count { it.mood.lowercase() in listOf("awful", "terrible", "sedih") }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            recentDiaries.forEach { diary ->
                if (diary.latitude != null && diary.longitude != null) {
                    val position = LatLng(diary.latitude, diary.longitude)
                    val (hue, emoji) = getMoodAttributes(diary.mood)

                    Marker(
                        state = MarkerState(position = position),
                        title = "$emoji ${diary.mood}",
                        snippet = diary.title,
                        icon = BitmapDescriptorFactory.defaultMarker(hue)
                    )
                }
            }
        }

        // --- KARTU STATISTIK (DENGAN 5 KATEGORI) ---
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
                Text("Jejak perasaanmu 7 hari terakhir", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Row agar muat 5 item
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    item { StatItem(Color(0xFF64B5F6), "Great", moodCounts["Great"] ?: 0) } // Biru
                    item { StatItem(Color(0xFFAED581), "Good", moodCounts["Good"] ?: 0) }   // Hijau
                    item { StatItem(Color(0xFFFFF176), "Okay", moodCounts["Neutral"] ?: 0) } // Kuning
                    item { StatItem(Color(0xFFFFB74D), "Bad", moodCounts["Bad"] ?: 0) }     // Oranye
                    item { StatItem(Color(0xFFE57373), "Awful", moodCounts["Awful"] ?: 0) } // Merah
                }
            }
        }

        FloatingActionButton(
            onClick = {
                val target = userLocation ?: tangerang
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, 15f))
            },
            containerColor = Color.White,
            contentColor = TechPrimary,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 110.dp, end = 16.dp)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location")
        }
    }
}

// --- KOMPONEN PENDUKUNG ---
@Composable
fun StatItem(color: Color, label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
        Text("$count", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TechTextPrimary)
    }
}

fun getMoodAttributes(mood: String): Pair<Float, String> {
    return when (mood.lowercase()) {
        "great", "amazing", "bahagia" -> Pair(BitmapDescriptorFactory.HUE_AZURE, "🤩") // Biru Langit
        "good", "senang" -> Pair(BitmapDescriptorFactory.HUE_GREEN, "🙂") // Hijau
        "neutral", "okay", "biasa" -> Pair(BitmapDescriptorFactory.HUE_YELLOW, "😐") // Kuning
        "bad", "buruk" -> Pair(BitmapDescriptorFactory.HUE_ORANGE, "😣") // Oranye
        "awful", "terrible", "sedih" -> Pair(BitmapDescriptorFactory.HUE_RED, "😭") // Merah
        else -> Pair(BitmapDescriptorFactory.HUE_VIOLET, "📍")
    }
}