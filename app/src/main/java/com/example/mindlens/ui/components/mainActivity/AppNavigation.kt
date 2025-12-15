package com.example.mindlens.ui.components.mainActivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mindlens.navigations.Routes
import com.example.mindlens.screens.MainScreen
import com.example.mindlens.screens.auth.NativeLoginScreen
import com.example.mindlens.screens.auth.RegisterOptionsScreen
import com.example.mindlens.screens.splash.OnboardingScreen
import com.example.mindlens.screens.splash.SplashScreen
import com.example.mindlens.viewModels.AuthViewModel

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