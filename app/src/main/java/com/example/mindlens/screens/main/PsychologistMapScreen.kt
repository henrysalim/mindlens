package com.example.mindlens.screens.main

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.ui.HomeViewModel
import coil.compose.AsyncImage
import com.example.mindlens.BuildConfig
import com.example.mindlens.ui.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Calendar
import kotlin.coroutines.EmptyCoroutineContext.get

// -----------------------------
// Model & Dummy Data (tidak berubah)
// -----------------------------
data class Psychologist(
    val id: String,
    val name: String,
    val specialty: String,
    val location: LatLng,
    val rating: Double,
    val distance: String,
    val isOpen: Boolean,
    val imageUrl: String,
    val address: String
)

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
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var nearbyPsychologists by remember { mutableStateOf<List<Psychologist>>(emptyList()) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var isFetchingPlaces by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // langsung fetch lokasi yang akurat
            scope.launch {
                userLocation = getAccurateLocation(context, fusedLocationClient)
                if (userLocation == null) {
                    Toast.makeText(context, "Gagal mendapatkan lokasi. Pastikan GPS ON.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "Location permission denied.", Toast.LENGTH_LONG).show()
        }
    }

    val hasLocationPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
        )
    }


    // camera state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakartaCenter, 13f)
    }

    // Launch on first composition: request permission if needed, else fetch
    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            userLocation = getAccurateLocation(context, fusedLocationClient)
        } else {
            // request permission (this will call the launcher lambda)
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
    LaunchedEffect(userLocation) {
        val location = userLocation ?: return@LaunchedEffect
        // avoid double fetch
        if (isFetchingPlaces) return@LaunchedEffect
        isFetchingPlaces = true

        scope.launch {
            try {
                val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=${location.latitude},${location.longitude}" +
                        "&radius=3000" +
                        "&keyword=psychologist" +
                        "&key=${BuildConfig.MAPS_API_KEY}"

                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()

                val body = withContext(Dispatchers.IO) {
                    val response = client.newCall(request).execute()
                    response.body?.string()
                }

                if (!body.isNullOrEmpty()) {
                    val json = JSONObject(body)
                    val results = json.optJSONArray("results")
                    val tempList = mutableListOf<Psychologist>()
                    if (results != null) {
                        for (i in 0 until results.length()) {
                            val place = results.getJSONObject(i)
                            val loc = place.getJSONObject("geometry").getJSONObject("location")
                            val lat = loc.getDouble("lat")
                            val lng = loc.getDouble("lng")

                            tempList.add(
                                Psychologist(
                                    id = place.optString("place_id", i.toString()),
                                    name = place.optString("name", "Unknown"),
                                    specialty = "Psychologist",
                                    location = LatLng(lat, lng),
                                    rating = place.optDouble("rating", 0.0),
                                    distance = "",
                                    isOpen = place.optJSONObject("opening_hours")?.optBoolean("open_now") ?: false,
                                    imageUrl = place.optJSONArray("photos")?.optJSONObject(0)?.optString("photo_reference") ?: "",
                                    address = place.optString("vicinity", "")
                                )
                            )
                        }
                    }
                    nearbyPsychologists = tempList
                }
            } catch (e: Exception) {
                Log.e("PlacesFetch", "Failed to fetch nearby places: ${e.message}", e)
            } finally {
                isFetchingPlaces = false
            }
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

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        // MapProperties: only enable my-location if we have permission
        val mapProperties = MapProperties(
            isMyLocationEnabled = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
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
                    val (hue, emoji) = getMoodAttributes(diary.mood)

                    Marker(
                        state = MarkerState(position = position),
                        title = "$emoji ${diary.mood}",
                        snippet = diary.title,
                        icon = BitmapDescriptorFactory.defaultMarker(hue)
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
// FilterChipItem & PsychologistDetailCard tetap sama (salin dari kode kamu)
@Composable
fun FilterChipItem(text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = TechPrimary,
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun PsychologistDetailCard(psy: Psychologist, onClose: () -> Unit, onNavigate: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = psy.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(psy.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TechTextPrimary)
                    Text(psy.specialty, fontSize = 14.sp, color = TechTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                        Text(" ${psy.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(" • ${psy.distance}", color = TechTextSecondary, fontSize = 12.sp)
                        if (psy.isOpen) {
                            Text(" • Open", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else {
                            Text(" • Closed", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = TechTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, null, tint = TechPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(psy.address, fontSize = 13.sp, color = TechTextSecondary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { /* TODO: Call Intent */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TechBackground, contentColor = TechPrimary)
                ) {
                    Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call")
                }
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                ) {
                    Icon(Icons.Default.Directions, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Route")
                }
            }
        }
    }
}
