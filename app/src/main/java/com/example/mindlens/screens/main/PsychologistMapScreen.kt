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

val psychologists = listOf(
    Psychologist("1", "Dr. Sarah Wijaya", "Clinical Psychologist", LatLng(-6.175, 106.823), 4.8, "0.5 km", true, "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=200", "Jl. Merdeka Barat No. 12"),
    Psychologist("2", "Dr. Budi Santoso", "Psychiatrist", LatLng(-6.178, 106.829), 4.5, "1.2 km", true, "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200", "Jl. Kebon Sirih No. 45"),
    Psychologist("3", "MindCare Center", "Counseling", LatLng(-6.171, 106.826), 4.9, "0.8 km", false, "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=200", "Jl. Majapahit No. 88"),
    Psychologist("4", "Amanda Putri, M.Psi", "Child Therapist", LatLng(-6.174, 106.832), 4.7, "1.5 km", true, "https://images.unsplash.com/photo-1594824476967-48c8b964273f?q=80&w=200", "Jl. Medan Merdeka Timur")
)

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
fun PsychologistMapScreen() {
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
            // markers from places
            nearbyPsychologists.forEach { psy ->
                Marker(
                    state = MarkerState(position = psy.location),
                    title = psy.name,
                    snippet = psy.specialty,
                    onClick = {
                        // open details
                        false
                    },
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(200f)
                )
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

        // Floating Search + Chips (UI unchanged)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                ) {
                    Icon(Icons.Default.Search, "Search", tint = TechTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search psychologist...", color = TechTextSecondary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Mic, "Voice", tint = TechPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Nearest", "Top Rated", "Open Now", "Specialist")) { filter ->
                    FilterChipItem(filter)
                }
            }
        }

        // Info Card & MyLocation FAB (keamanan: gunakan userLocation null check)
        var selectedPsychologist by remember { mutableStateOf<Psychologist?>(null) }

        AnimatedVisibility(
            visible = selectedPsychologist != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .padding(horizontal = 16.dp)
        ) {
            selectedPsychologist?.let { psy ->
                PsychologistDetailCard(
                    psy = psy,
                    onClose = { selectedPsychologist = null },
                    onNavigate = {
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(psy.location, 16f))
                    }
                )
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
