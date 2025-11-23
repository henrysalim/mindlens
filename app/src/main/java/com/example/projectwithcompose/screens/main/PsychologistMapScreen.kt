package com.example.projectwithcompose.screens.main

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.projectwithcompose.ui.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// --- 1. DATA MODEL PSIKOLOG ---
data class Psychologist(
    val id: String,
    val name: String,
    val specialty: String,
    val location: LatLng, // Koordinat Google Maps
    val rating: Double,
    val distance: String,
    val isOpen: Boolean,
    val imageUrl: String,
    val address: String
)

// --- 2. DUMMY DATA (Disebar di sekitar Monas Jakarta biar kelihatan) ---
val jakartaCenter = LatLng(-6.175392, 106.827153) // Monas

val psychologists = listOf(
    Psychologist("1", "Dr. Sarah Wijaya", "Clinical Psychologist", LatLng(-6.175, 106.823), 4.8, "0.5 km", true, "https://images.unsplash.com/photo-1559839734-2b71ea197ec2?q=80&w=200", "Jl. Merdeka Barat No. 12"),
    Psychologist("2", "Dr. Budi Santoso", "Psychiatrist", LatLng(-6.178, 106.829), 4.5, "1.2 km", true, "https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?q=80&w=200", "Jl. Kebon Sirih No. 45"),
    Psychologist("3", "MindCare Center", "Counseling", LatLng(-6.171, 106.826), 4.9, "0.8 km", false, "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=200", "Jl. Majapahit No. 88"),
    Psychologist("4", "Amanda Putri, M.Psi", "Child Therapist", LatLng(-6.174, 106.832), 4.7, "1.5 km", true, "https://images.unsplash.com/photo-1594824476967-48c8b964273f?q=80&w=200", "Jl. Medan Merdeka Timur")
)

@Composable
fun PsychologistMapScreen() {
    // State Kamera Map (Start di Jakarta)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(jakartaCenter, 14f)
    }

    // State untuk Psikolog yang dipilih (diklik markernya)
    var selectedPsychologist by remember { mutableStateOf<Psychologist?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        // --- 3. GOOGLE MAPS ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false) // UI Bersih
        ) {
            // Render Marker untuk setiap Psikolog
            psychologists.forEach { psy ->
                Marker(
                    state = MarkerState(position = psy.location),
                    title = psy.name,
                    snippet = psy.specialty,
                    onClick = {
                        selectedPsychologist = psy // Simpan data yang diklik
                        false // Return false agar default behavior (center camera) tetap jalan
                    },
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                        if (selectedPsychologist == psy) 120f else 200f // Ganti warna marker jika dipilih (Hue)
                    )
                )
            }
        }

        // --- 4. FLOATING SEARCH BAR (ATAS) ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            // Search Input
            Card(
                shape = RoundedCornerShape(30.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize()
                ) {
                    Icon(Icons.Default.Search, "Search", tint = TechTextSecondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Search psychologist...", color = TechTextSecondary, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Mic, "Voice", tint = TechPrimary)
                }
            }

            // Filter Chips (Scrollable)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Nearest", "Top Rated", "Open Now", "Specialist")) { filter ->
                    FilterChipItem(filter)
                }
            }
        }

        // --- 5. FLOATING INFO CARD (BAWAH) ---
        // Animasi: Muncul dari bawah saat marker diklik
        AnimatedVisibility(
            visible = selectedPsychologist != null,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp) // Padding agar tidak tertutup Navbar
                .padding(horizontal = 16.dp)
        ) {
            selectedPsychologist?.let { psy ->
                PsychologistDetailCard(
                    psy = psy,
                    onClose = { selectedPsychologist = null },
                    onNavigate = {
                        // Arahkan kamera map ke lokasi dia
                        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(psy.location, 16f))
                    }
                )
            }
        }

        // Tombol "My Location" (Floating)
        if (selectedPsychologist == null) {
            FloatingActionButton(
                onClick = { cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(jakartaCenter, 14f)) },
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
}

// --- KOMPONEN UI ---

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
            // Baris Atas: Gambar + Info
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

            // Alamat
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Outlined.LocationOn, null, tint = TechPrimary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(psy.address, fontSize = 13.sp, color = TechTextSecondary)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Aksi
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
                    onClick = onNavigate, // Zoom in map
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