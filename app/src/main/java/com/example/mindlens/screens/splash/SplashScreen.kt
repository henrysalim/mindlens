package com.example.mindlens.screens.splash

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
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import com.example.mindlens.navigations.Routes
import com.example.mindlens.viewModels.AuthState
import com.example.mindlens.viewModels.AuthViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.em
import com.example.mindlens.ui.PrimaryGreen

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()

    // Create a state to track if the animation is done
    var isAnimationFinished by remember { mutableStateOf(false) }

    // Start the Timer
    LaunchedEffect(key1 = true) {
        delay(1500) // Minimum wait time (1.5 seconds)
        isAnimationFinished = true
    }

    // Start the Auth Check
    LaunchedEffect(key1 = true) {
        viewModel.checkAuthStatus()
    }

    // Navigation Logic
    LaunchedEffect(isAnimationFinished, authState) {
        // Only proceed if the animation is done AND we are not loading
        if (isAnimationFinished && authState !is AuthState.Loading) {

            when (authState) {
                is AuthState.Authenticated -> {
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
                is AuthState.Unauthenticated -> {
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
                else -> {  }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "MindLens", fontWeight = FontWeight.Bold, color = PrimaryGreen, fontSize = 8.em)
    }
}