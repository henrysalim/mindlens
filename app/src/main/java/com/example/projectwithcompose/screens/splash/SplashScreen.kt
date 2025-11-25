package com.example.projectwithcompose.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import com.example.projectwithcompose.Routes
import com.example.projectwithcompose.viewModel.AuthState
import com.example.projectwithcompose.viewModel.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.projectwithcompose.ui.PrimaryGreen // Pastikan import warna sesuai tema

@Composable
fun SplashScreen(
    viewModel: AuthViewModel = viewModel(),
    onSplashFinished: (String) -> Unit // Ubah parameter agar bisa kirim tujuan rute
) {
    val authState by viewModel.authState.collectAsState()
    var isTimerFinished by remember { mutableStateOf(false) }

    // 1. Mulai Timer & Cek Auth secara bersamaan
    LaunchedEffect(key1 = true) {
        viewModel.checkAuthStatus() // Cek ke Supabase
        delay(1500) // Tunggu minimal 1.5 detik agar logo terlihat
        isTimerFinished = true
    }

    // 2. Logika Navigasi (Jalan ketika Timer selesai & Auth State didapat)
    LaunchedEffect(isTimerFinished, authState) {
        if (isTimerFinished) {
            when (authState) {
                is AuthState.Authenticated -> {
                    // Jika sudah login, langsung ke Dashboard Utama
                    onSplashFinished(Routes.MainApp)
                }
                else -> {
                    // Jika belum login / error / loading selesai, ke Onboarding
                    onSplashFinished(Routes.Onboarding)
                }
            }
        }
    }

    // 3. UI Splash
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Ganti dengan Logo Anda jika ada
        Text(
            text = "MindLens",
            fontWeight = FontWeight.Bold,
            color = PrimaryGreen // Sesuaikan warna
        )
    }
}