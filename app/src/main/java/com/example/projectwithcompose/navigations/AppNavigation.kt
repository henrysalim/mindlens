package com.example.projectwithcompose.navigations

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.Routes
import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
import com.example.projectwithcompose.screens.main.HomeScreen
import com.example.projectwithcompose.screens.main.MainScreen
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen

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

        // 4. Home
        composable(Routes.MainApp) {
            MainScreen() // Your main app content
        }
    }
}
