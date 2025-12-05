package com.example.mindlens.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindlens.Routes
import com.example.mindlens.screens.auth.NativeLoginScreen
import com.example.mindlens.screens.auth.RegisterOptionsScreen
import com.example.mindlens.screens.main.MainScreen
import com.example.mindlens.screens.splash.OnboardingScreen
import com.example.mindlens.screens.splash.SplashScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash
        composable(Routes.Splash) {
            SplashScreen(
                navController
            )
        }

        // 2. Onboarding
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Auth (NEW)
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

        // 4. Home
        composable(Routes.MainApp) {
            MainScreen() // Your main app content
        }
    }
}
