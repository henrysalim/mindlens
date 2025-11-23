package com.example.projectwithcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.screens.main.MainScreen
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
    const val MainApp = "main_app" // Ganti nama route 'Home' jadi 'MainApp' biar jelas
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
                    // Masuk ke Aplikasi Utama (MainScreen) dan hapus onboarding dari history
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Main App Route (Wadah Utama dengan Navbar)
        composable(Routes.MainApp) {
            // PENTING: Di sini kita panggil MainScreen(), BUKAN HomeScreen().
            // MainScreen() yang akan mengatur BottomBar dan menampilkan HomeScreen di dalamnya.
            MainScreen()
        }
    }
}