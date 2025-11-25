//package com.example.projectwithcompose.navigations
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.projectwithcompose.Routes
//import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
//import com.example.projectwithcompose.screens.main.HomeScreen
//import com.example.projectwithcompose.screens.splash.OnboardingScreen
//import com.example.projectwithcompose.screens.splash.SplashScreen
//
//@Composable
//fun AppNavigation() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = Routes.Splash) {
//
//        // 1. Splash
//        composable(Routes.Splash) {
//            SplashScreen(
//                // Callback ini menerima rute tujuan (apakah ke Onboarding atau langsung Home)
//                onSplashFinished = { nextRoute ->
//                    navController.navigate(nextRoute) {
//                        popUpTo(Routes.Splash) { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 2. Onboarding
//        composable(Routes.Onboarding) {
//            OnboardingScreen(
//                onOnboardingFinished = {
//                    navController.navigate(Routes.MainApp) {
//                        popUpTo(Routes.Onboarding) { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 3. Auth (NEW)
//        composable(Routes.RegisterOptions) {
//            RegisterOptionsScreen(
//                navController = navController,
//            )
//        }
//
//        // 4. Home
//        composable(Routes.MainApp) {
//            HomeScreen() // Your main app content
//        }
//    }
//}

package com.example.projectwithcompose.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.Routes
import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
import com.example.projectwithcompose.screens.main.MainScreen // Pastikan Import ini
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash
        composable(Routes.Splash) {
            SplashScreen(
                // PERBAIKAN UTAMA DI SINI:
                // Gunakan '_' untuk MENGABAIKAN rute yang dikirim oleh SplashScreen
                onSplashFinished = { _ ->
                    // KITA PAKSA PINDAH KE MAIN APP
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Force ke Main App juga dari sini
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Auth (Disimpan dulu)
        composable(Routes.RegisterOptions) {
            RegisterOptionsScreen(
                navController = navController,
            )
        }

        // 4. Home / Main App
        composable(Routes.MainApp) {
            // PERBAIKAN: Panggil MainScreen() agar Navbar muncul
            // Jangan panggil HomeScreen() langsung
            MainScreen()
        }
    }
}