package com.example.mindlens.screens.profile // Sesuaikan package

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mindlens.ui.*
import com.example.mindlens.viewModel.AuthViewModel

@Composable
fun ProfileScreen(
    onNavigateToEdit: () -> Unit,
    onNavigateToPassword: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToTnc: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val userName = remember { authViewModel.getUserName() }
    val email = remember { authViewModel.getEmail() }

    Box(modifier = Modifier.fillMaxSize().background(TechBackground)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // 1. HEADER PROFILE (Mirip Home)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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
                        .padding(horizontal = 24.dp)
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
                        Box {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                            )
                            // Edit Icon Kecil
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(TechPrimary)
                                    .clickable { onNavigateToEdit() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Outlined.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechTextPrimary)
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
                Text("Account Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(Icons.Outlined.Person, "Edit Profile", onNavigateToEdit)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileMenuItem(Icons.Outlined.Lock, "Change Password", onNavigateToPassword)

                Spacer(modifier = Modifier.height(24.dp))

                Text("Support & Legal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(Icons.Outlined.Info, "About MindLens", onNavigateToAbout)
                Spacer(modifier = Modifier.height(8.dp))
                ProfileMenuItem(Icons.Outlined.Description, "Terms & Conditions", onNavigateToTnc)

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE), contentColor = Color(0xFFD32F2F)),
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

// --- KOMPONEN PENDUKUNG UI ---

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TechTextSecondary)
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TechPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = TechTextPrimary)
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}