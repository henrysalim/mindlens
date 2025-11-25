package com.example.projectwithcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.data.Article
import com.example.projectwithcompose.navigations.Screen
import com.example.projectwithcompose.screens.article.ArticleDetailScreen
import com.example.projectwithcompose.screens.article.ArticleScreen
import com.example.projectwithcompose.screens.auth.RegisterOptionsScreen
import com.example.projectwithcompose.screens.main.MainScreen
import com.example.projectwithcompose.screens.splash.OnboardingScreen
import com.example.projectwithcompose.screens.splash.SplashScreen
import com.example.projectwithcompose.ui.DailyDiaryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Gunakan false agar warna hijau konsisten sesuai desain Figma
            DailyDiaryTheme(dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}

object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val MainApp = "main_app"
    const val RegisterOptions = "register-options"
    const val EmailRegister = "email-register"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Screen
        composable(Routes.Splash) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.Onboarding) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }

        // 2. Onboarding Screen
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    // Arahkan ke RegisterOptions setelah onboarding
                    navController.navigate(Routes.MainApp) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Register Options (Layar Auth)
        composable(Routes.RegisterOptions) {
            // [PENTING] Pastikan RegisterOptionsScreen di file aslinya
            // memang menerima parameter 'navController'.
            RegisterOptionsScreen(
                navController = navController
            )
        }

        // 4. Main App (Dashboard dengan Navbar)
        composable(Routes.MainApp) {
            MainScreen()
        }

        // (Opsional) Jika ada rute EmailRegister
        composable(Routes.EmailRegister) {
            // Pastikan screen ini juga sudah dibuat atau diimport
            // EmailRegisterScreen(navController = navController)
        }

        composable(Screen.Articles.route) {
            ArticleScreen(
                onArticleClick = { article ->
                    // Pass data via SavedStateHandle
                    navController.currentBackStackEntry?.savedStateHandle?.set("article", article)
                    navController.navigate(Screen.ArticleDetail.route)
                }
            )
        }

        composable(Screen.ArticleDetail.route) {
            // Retrieve data
            val article = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<Article>("article")

            if (article != null) {
                ArticleDetailScreen(article = article, onBack = { navController.popBackStack() })
            }
        }
    }
}