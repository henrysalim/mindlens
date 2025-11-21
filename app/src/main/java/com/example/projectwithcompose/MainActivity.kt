package com.example.projectwithcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.screens.main.HomeScreen
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen
import com.example.projectwithcompose.ui.DailyDiaryTheme

// Import the composables and data classes you just created above

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply your app theme here if you have one
            DailyDiaryTheme {
                AppNavigation()
            }
        }
    }
}

// Define your route names
object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Home = "home" // Where you go after onboarding
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Route
        composable(Routes.Splash) {
            SplashScreen(
                onSplashFinished = {
                    // Pop splash off backstack so back button doesn't return to it
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Route
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Navigate to Home and pop onboarding off backstack
                    navController.navigate(Routes.Home) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Your actual App Home Screen Route
        composable(Routes.Home) {
            // Replace this with your actual Home Screen Composable
            HomeScreen()
        }
    }
}