package com.example.mindlens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindlens.screens.auth.NativeLoginScreen
import com.example.mindlens.screens.auth.RegisterOptionsScreen
import com.example.mindlens.screens.MainScreen
import com.example.mindlens.screens.splash.OnboardingScreen
import com.example.mindlens.screens.splash.SplashScreen
import com.example.mindlens.ui.DailyDiaryTheme
import com.example.mindlens.viewModels.AuthViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyDiaryTheme(dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}

// define routes
object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val MainApp = "main_app"
    const val RegisterOptions = "register-options"
    const val NativeLogin = "login"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val rootNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = rootNavController, startDestination = Routes.Splash) {

        // 1. Splash Screen
        composable(Routes.Splash) {
            SplashScreen(
                navController = rootNavController,
            )
        }

        // 2. Onboarding Screen
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    rootNavController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Register Options
        composable(Routes.RegisterOptions) {
            RegisterOptionsScreen(
                navController = rootNavController
            )
        }

        // 4. Login
        composable(Routes.NativeLogin) {
            NativeLoginScreen(
                navController = rootNavController
            )
        }

        // 5. Main App (Dashboard)
        composable(Routes.MainApp) {
            MainScreen(
                onLogout = {
                    authViewModel.signOut()
                    rootNavController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.MainApp) { inclusive = true }
                    }
                }
            )
        }
    }
}