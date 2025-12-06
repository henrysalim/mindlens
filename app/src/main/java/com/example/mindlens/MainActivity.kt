package com.example.mindlens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mindlens.data.Article
import com.example.mindlens.data.DiaryEntry
import com.example.mindlens.navigations.Screen
import com.example.mindlens.screens.article.ArticleDetailScreen
import com.example.mindlens.screens.article.ArticleScreen
import com.example.mindlens.screens.auth.NativeLoginScreen
import com.example.mindlens.screens.auth.RegisterOptionsScreen
import com.example.mindlens.screens.main.DiaryDetailScreen
import com.example.mindlens.screens.main.MainScreen
import com.example.mindlens.screens.splash.OnboardingScreen
import com.example.mindlens.screens.splash.SplashScreen
import com.example.mindlens.ui.DailyDiaryTheme
import kotlinx.serialization.json.Json

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
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.Splash) {

        // 1. Splash Screen
        composable(Routes.Splash) {
            SplashScreen(
                navController = navController,
            )
        }

        // 2. Onboarding Screen
        composable(Routes.Onboarding) {
            OnboardingScreen(
                onOnboardingFinished = {
                    navController.navigate(Routes.RegisterOptions) {
                        popUpTo(Routes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        // 3. Register Options (Layar Auth)
        composable(Routes.RegisterOptions) {
            RegisterOptionsScreen(
                navController = navController
            )
        }

        composable(Routes.NativeLogin) {
            NativeLoginScreen(
                navController = navController
            )
        }

        // 4. Main App (Dashboard dengan Navbar)
        composable(Routes.MainApp) {
            MainScreen()
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

        composable(
            route = "detail/{entry}",
            arguments = listOf(navArgument("entry") { type = NavType.StringType })
        ) { backStackEntry ->
            // Retrieve and Deserialize the object
            val entryJson = backStackEntry.arguments?.getString("entry")
            val entry = entryJson?.let { Json.decodeFromString<DiaryEntry>(it) }

            if (entry != null) {
                DiaryDetailScreen(
                    entry = entry,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}