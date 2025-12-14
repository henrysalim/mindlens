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
import androidx.compose.foundation.lazy.LazyRow
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


val jakartaCenter = LatLng(-6.175392, 106.827153)

// -----------------------------
// Helper: getAccurateLocation (suspend)
// -----------------------------
private suspend fun getAccurateLocation(
    context: Context,
    fused: FusedLocationProviderClient
): LatLng? {
    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
        PackageManager.PERMISSION_GRANTED
    ) return null

    return try {
        // Recommended: getCurrentLocation for a fresh location
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

// -----------------------------
// Main Composable: PsychologistMapScreen (safe version)
// -----------------------------
@Composable
fun PsychologistMapScreen(viewModel: HomeViewModel) {
    var isLocationPermanentlyDenied by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isFetchingPlaces by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

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

    // Launch on first composition: request permission if needed, else fetch
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

    // Fetch nearby places when userLocation available



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

    // UI
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
            uiSettings = uiSettings
        ) {
            recentDiaries.forEach { diary ->
                if (diary.latitude != null && diary.longitude != null) {
                    val position = LatLng(diary.latitude, diary.longitude)
                    val (iconRes, color) = getMoodVectorIcon(diary.mood)

                    Marker(
                        state = MarkerState(position = position),
                        title = diary.mood,
                        snippet = diary.title,
                        icon = bitmapDescriptorFromVector(
                            context = context,
                            vectorResId = iconRes,
                            tint = color
                        )
                    )
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
                        // 👉 buka settings
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null)
                        )
                        context.startActivity(intent)
                    } else {
                        // 👉 request permission normal
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

// HELPER VECTOR DRAWABLE SENTIMENT
fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    tint: Color
): BitmapDescriptor {

    val drawable = ContextCompat.getDrawable(context, vectorResId)
        ?: return BitmapDescriptorFactory.defaultMarker()

    drawable.setTint(tint.toArgb())

    val sizePx = 64
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}


// AMBIL ICON SENTIMENT

fun getMoodVectorIcon(mood: String): Pair<Int, Color> {
    return when (mood.lowercase()) {
        "great", "amazing", "bahagia" ->
            R.drawable.ic_verysatisfied to Color(0xFF64B5F6)

        "good", "senang" ->
            R.drawable.ic_satisfied to Color(0xFFAED581)

        "neutral", "okay", "biasa" ->
            R.drawable.ic_neutral to Color(0xFFFFF176)

        "bad", "buruk" ->
            R.drawable.ic_dissatisfied to Color(0xFFFFB74D)

        "awful", "terrible", "sedih" ->
            R.drawable.ic_verydissatisfied to Color(0xFFE57373)

        else ->
            R.drawable.ic_neutral to Color.Gray
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
