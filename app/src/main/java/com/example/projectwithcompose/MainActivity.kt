package com.example.projectwithcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
import com.example.projectwithcompose.screens.main.HomeScreen
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen
import com.example.projectwithcompose.ui.DailyDiaryTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the app theme
            DailyDiaryTheme {
                AppNavigation()
            }
        }
    }
}

// Define route names
object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val RegisterOptions = "register-options"
    const val EmailRegister = "email-register"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var emailVal = ""
    var passwordVal = ""

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Route
        composable(Routes.Splash) {
            SplashScreen(
                navController = navController,
            )
        }

        // 2. Onboarding Route
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

        // 3. handle auth
        composable(Routes.RegisterOptions) {
            RegisterOptionsScreen(
                navController = navController,
            )
        }

        // 4. Actual App Home Screen Route
        composable(Routes.Home) {
            HomeScreen()
        }
    }
}