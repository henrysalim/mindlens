package com.example.mindlens.screens

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Import ini
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.mindlens.navigations.Screen
import com.example.mindlens.viewModels.HomeViewModel // Import ViewModel
import com.example.mindlens.ui.PrimaryGreen
import com.example.mindlens.ui.TextBlack
import com.example.mindlens.model.Article
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.screens.article.ArticleScreen
import com.example.mindlens.screens.article.ArticleDetailScreen
import com.example.mindlens.screens.depressionClassifier.DepressionClassifierScreen
import com.example.mindlens.screens.home.ActivityDetailScreen
import com.example.mindlens.screens.home.BreathingScreen
import com.example.mindlens.screens.home.DiaryDetailScreen
import com.example.mindlens.screens.home.MoodScreen
import com.example.mindlens.screens.map.PsychologistMapScreen
import com.example.mindlens.screens.home.VideoPlayerScreen
import com.example.mindlens.screens.main.HomeScreen
import com.example.mindlens.screens.profile.*
import com.example.mindlens.screens.profile.subprofile.AboutAppScreen
import com.example.mindlens.screens.profile.subprofile.TermsConditionsScreen
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()

    // --- 1. BUAT VIEWMODEL DI SINI (SHARED STATE) ---
    // Ini kuncinya! Kita buat 1 ViewModel untuk dipakai ramai-ramai.
    val sharedViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    val items = listOf(
        Screen.Home to Icons.Outlined.Home,
        Screen.PsychologistMap to Icons.Outlined.LocationOn,
        Screen.DepressionDetection to Icons.Outlined.DocumentScanner,
        Screen.Articles to Icons.Outlined.Article,
        Screen.Profile to Icons.Outlined.Person
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = items.any { it.first.route == currentDestination?.route }

            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    items.forEach { (screen, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = null,
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TextBlack,
                                unselectedIconColor = Color.Gray,
                                indicatorColor = PrimaryGreen
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- HOME SCREEN ---
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = sharedViewModel, // <-- Oper ViewModel yang sama
                    onNavigateToHistory = { navController.navigate(Screen.DiaryHistory.route) },
                    onNavigateToActivity = { type -> navController.navigate(Screen.ActivityDetail.createRoute(type)) },
                    onNavigateToBreathing = { navController.navigate(Screen.BreathingExercise.route) },
                    onNavigateToScan = {
                        navController.navigate(Screen.DepressionDetection.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToDetail = { entry ->
                        val entryJson = Json.encodeToString(entry)
                        val encodedJson = Uri.encode(entryJson)
                        navController.navigate(Screen.DiaryDetail.createRoute(encodedJson))
                    }
                )
            }

            // --- MAP SCREEN (EMOTIONAL JOURNEY) ---
            composable(Screen.PsychologistMap.route) {
                PsychologistMapScreen(
                    viewModel = sharedViewModel // <-- Oper ViewModel yang sama, jadi dia tahu kalau ada data baru!
                )
            }

            // ... (Kode routing lainnya tetap sama, tidak perlu diubah) ...

            composable(Screen.DepressionDetection.route) { DepressionClassifierScreen() }

            composable(Screen.Articles.route) {
                ArticleScreen(onArticleClick = { article ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("article_data", article)
                    navController.navigate(Screen.ArticleDetail.route)
                })
            }

            composable(Screen.ArticleDetail.route) {
                val article = navController.previousBackStackEntry?.savedStateHandle?.get<Article>("article_data")
                if (article != null) ArticleDetailScreen(article = article, onBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                    onNavigateToTnc = { navController.navigate(Screen.TermsConditions.route) },
                    onLogout = onLogout
                )
            }
            // ... Sub-screen Profile (Edit, Password, dll) ...
            composable(Screen.EditProfile.route) { EditProfileScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.AboutApp.route) { AboutAppScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.TermsConditions.route) { TermsConditionsScreen(onBack = { navController.popBackStack() }) }

            // ... History & Detail ...
            composable(Screen.DiaryHistory.route) {
                MoodScreen(
                    viewModel = sharedViewModel, // MoodScreen juga boleh pakai shared jika perlu
                    onBackClick = { navController.popBackStack() },
                    onItemClick = { entry: DiaryEntry ->
                        val entryJson = Json.encodeToString(entry)
                        val encodedJson = Uri.encode(entryJson)
                        navController.navigate(Screen.DiaryDetail.createRoute(encodedJson))
                    }
                )
            }

            composable(
                route = Screen.DiaryDetail.route,
                arguments = listOf(navArgument("entry") { type = NavType.StringType })
            ) { backStackEntry ->
                val entryJson = backStackEntry.arguments?.getString("entry")
                val entry = entryJson?.let { Json.decodeFromString<DiaryEntry>(it) }
                if (entry != null) DiaryDetailScreen(entry = entry, onBackClick = { navController.popBackStack() })
            }

            // ... Activity ...
            composable(Screen.ActivityDetail.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "Activity"
                ActivityDetailScreen(
                    activityType = type,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { vid, tit, desc ->
                        navController.navigate(Screen.VideoPlayer.createRoute(vid, tit, desc))
                    })
            }

            composable(Screen.VideoPlayer.route) { backStackEntry ->
                val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val desc = backStackEntry.arguments?.getString("desc") ?: ""
                VideoPlayerScreen(videoId, title, desc, onBack = { navController.popBackStack() })
            }

            composable(Screen.BreathingExercise.route) {
                BreathingScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}