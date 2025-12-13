package com.example.mindlens.screens.profile // Sesuaikan package

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.profile.ProfileMenuItem
import com.example.mindlens.ui.components.profile.ProfileStat
import com.example.mindlens.viewModels.AuthViewModel
import com.example.mindlens.viewModels.EditProfileViewModel

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTnc: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: EditProfileViewModel = viewModel()
) {
    val userName = remember { authViewModel.getUserName() }
    val email = remember { authViewModel.getEmail() }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    Box(modifier = Modifier.fillMaxSize().background(TechBackground)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            // 1. HEADER PROFILE (Mirip Home)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                // Gradient Background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .background(Brush.linearGradient(listOf(TechPrimary, Color(0xFF004E55))))
                )

                // Profile Card Melayang
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Foto Profil
                        Box(
                            contentAlignment = Alignment.BottomEnd,
                            modifier = Modifier
                                .size(100.dp)
                        ) {
                            // Logic: Show New Selection -> OR Show Saved Base64 -> OR Show Google URL -> OR Placeholder
                            if (!profileViewModel.currentAvatarBase64.value.isNullOrEmpty()) {
                                // Convert Base64 string to Bitmap for Display
                                val cleanBase64 = profileViewModel.currentAvatarBase64.value!!
                                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val username = profileViewModel.name.value
                                AsyncImage(
                                    model = profileViewModel.googleAvatarUrl.value
                                        ?: "https://ui-avatars.com/api/?name=${username}&background=random&color=fff",
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TechTextPrimary
                        )
                        Text(email, style = MaterialTheme.typography.bodyMedium, color = TechTextSecondary)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Row (Opsional - Biar Canggih)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProfileStat("12", "Scans")
                            ProfileStat("85%", "Wellness")
                            ProfileStat("24", "Diaries")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. MENU OPTIONS
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Account Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(Icons.Outlined.Person, "Edit Profile", onNavigateToEdit)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Support & Legal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(Icons.Outlined.Info, "About MindLens", onNavigateToAbout)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileMenuItem(Icons.Outlined.Description, "Terms & Conditions", onNavigateToTnc)

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Color(0xFFD32F2F)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Outlined.Logout, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(100.dp)) // Bottom Padding
            }
        }
    }
}