package com.example.mindlens.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.mindlens.Routes
import com.example.mindlens.navigations.Screen
import com.example.mindlens.ui.PrimaryGreen
import com.example.mindlens.ui.TextBlack

// --- IMPORT SCREENS ---
import com.example.mindlens.screens.article.ArticleScreen
import com.example.mindlens.screens.article.ArticleDetailScreen
import com.example.mindlens.data.Article
import com.example.mindlens.screens.profile.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Update Icon Scan jadi DocumentScanner (biar iconnya Analyzed)
    val items = listOf(
        Screen.Home to Icons.Outlined.Home,
        Screen.PsychologistMap to Icons.Outlined.LocationOn,
        Screen.DepressionDetection to Icons.Outlined.DocumentScanner, // Masuk ke Navbar
        Screen.Articles to Icons.Outlined.Article,
        Screen.Profile to Icons.Outlined.Person
    )

    Scaffold(
        // 1. FAB DIHAPUS total agar tidak ada tombol melayang

        // 2. Bottom Navigation Bar Normal (5 Item Sejajar)
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Loop biasa tanpa skip index
                items.forEach { (screen, icon) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = null) },
                        label = null, // Label tetap hidden biar minimalis
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextBlack,
                            unselectedIconColor = Color.Gray,
                            // Highlight Hijau saat dipilih
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 1. HOME SCREEN ---
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToHistory = { navController.navigate(Screen.DiaryHistory.route) },
                    onNavigateToActivity = { type -> navController.navigate(Screen.ActivityDetail.createRoute(type)) },
                    onNavigateToBreathing = { navController.navigate(Screen.BreathingExercise.route) }
                )
            }

            // --- 2. MAIN FEATURES ---
            composable(Screen.PsychologistMap.route) { PsychologistMapScreen() }
            composable(Screen.DepressionDetection.route) { DepressionClassifierScreen() }

            // --- 3. ARTIKEL (LIST & DETAIL) ---
            composable(Screen.Articles.route) {
                ArticleScreen(
                    onArticleClick = { article ->
                        navController.currentBackStackEntry?.savedStateHandle?.set("article_data", article)
                        navController.navigate(Screen.ArticleDetail.route)
                    }
                )
            }

            composable(Screen.ArticleDetail.route) {
                val article = navController.previousBackStackEntry?.savedStateHandle?.get<Article>("article_data")
                if (article != null) {
                    ArticleDetailScreen(
                        article = article,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // --- 4. PROFILE & SETTINGS ---
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToPassword = { navController.navigate(Screen.ChangePassword.route) },
                    onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                    onNavigateToTnc = { navController.navigate(Screen.TermsConditions.route) },
                    onLogout = {
                        navController.navigate(Routes.RegisterOptions) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Sub-halaman Profile
            composable(Screen.EditProfile.route) { EditProfileScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.ChangePassword.route) { ChangePasswordScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.AboutApp.route) { AboutAppScreen(onBack = { navController.popBackStack() }) }
            composable(Screen.TermsConditions.route) { TermsConditionsScreen(onBack = { navController.popBackStack() }) }

            // --- 5. ACTIVITY & VIDEO FEATURES ---
            composable(Screen.ActivityDetail.route) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "Activity"
                ActivityDetailScreen(
                    activityType = type,
                    onBack = { navController.popBackStack() },
                    onVideoClick = { videoId, title, desc ->
                        navController.navigate(Screen.VideoPlayer.createRoute(videoId, title, desc))
                    }
                )
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

            composable(Screen.ExerciseGuide.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                ExerciseGuideScreen(exerciseId = id, onBack = { navController.popBackStack() })
            }

            composable(Screen.DiaryHistory.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Full History Page") }
            }
        }
    }
}