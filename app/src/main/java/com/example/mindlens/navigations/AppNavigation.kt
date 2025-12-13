package com.example.mindlens.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindlens.Routes
import com.example.mindlens.screens.auth.NativeLoginScreen
import com.example.mindlens.screens.auth.RegisterOptionsScreen
import com.example.mindlens.screens.MainScreen
import com.example.mindlens.screens.splash.OnboardingScreen
import com.example.mindlens.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Screen
        composable(Routes.Splash) {
            SplashScreen(
                navController = navController
            )
        }

        // 2. Onboarding Screen
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Setelah Onboarding, user harus Login/Register dulu (bukan langsung MainApp)
                    navController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Auth Screens
        composable(Routes.RegisterOptions) {
            RegisterOptionsScreen(
                navController = navController,
            )
        }

        composable(Routes.NativeLogin) {
            NativeLoginScreen(
                navController = navController,
            )
        }

        // 4. Main App (Dashboard)
        composable(Routes.MainApp) {
            // PERBAIKAN DI SINI:
            // Kita wajib mengisi parameter onLogout
            MainScreen(
                onLogout = {
                    // Logika saat user klik Logout di Profile:
                    // Kembali ke halaman RegisterOptions dan hapus history navigasi
                    navController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.MainApp) { inclusive = true }
                    }
                }
            )
        }
    }
}