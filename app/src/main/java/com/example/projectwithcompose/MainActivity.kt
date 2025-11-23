package com.example.projectwithcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.screens.main.MainScreen
import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
import com.example.projectwithcompose.screens.main.HomeScreen
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen
import com.example.projectwithcompose.ui.DailyDiaryTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Gunakan Theme baru kita (dynamicColor = false agar warna hijau konsisten)
            DailyDiaryTheme(dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}

// Definisi Route Sederhana untuk alur awal (Splash -> Onboarding -> Main App)
object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val MainApp = "main_app"
    const val RegisterOptions = "register-options"
    const val EmailRegister = "email-register"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var emailVal = ""
    var passwordVal = ""

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Screen Route
        composable(Routes.Splash) {
            SplashScreen(
                onSplashFinished = {
                    // Hapus Splash dari backstack agar tidak bisa kembali ke splash
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen Route
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Navigate to Home and pop onboarding off backstack
                    navController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        // 3. Register Options (Layar Auth Teman)
        composable(Routes.RegisterOptions) {
            // Pastikan di dalam codingan teman (RegisterOptionsScreen), 
            // kalau login sukses navigasinya ke arah "main_app" (Routes.MainApp)
            RegisterOptionsScreen(
                navController = navController
            )
        }

        // 4. Main App (Dashboard Kamu dengan Navbar)
        composable(Routes.MainApp) {
            MainScreen()
        }
    }
}