package com.example.projectwithcompose.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.projectwithcompose.navigations.Screen
import com.example.projectwithcompose.screens.profile.* // Import Profile Screens
import com.example.projectwithcompose.ui.PrimaryGreen // Pastikan import dari theme
import com.example.projectwithcompose.ui.TextBlack   // Pastikan import dari theme

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home to Icons.Outlined.Home,
        Screen.PsychologistMap to Icons.Outlined.LocationOn,
        Screen.DepressionDetection to Icons.Outlined.CameraAlt,
        Screen.Articles to Icons.Outlined.Article,
        Screen.Profile to Icons.Outlined.Person
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.DepressionDetection.route) },
                containerColor = PrimaryGreen,
                contentColor = TextBlack,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 8.dp)
            ) { Icon(Icons.Default.Add, "Scan") }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEachIndexed { index, (screen, icon) ->
                    if (index == 2) {
                        NavigationBarItem(selected = false, onClick = {}, icon = {}, enabled = false)
                        return@forEachIndexed
                    }
                    NavigationBarItem(
                        icon = { Icon(icon, null) },
                        label = null,
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TextBlack,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = PrimaryGreen.copy(alpha = 0.3f)
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
            // --- 1. HOME & MAIN FEATURES ---
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToHistory = { navController.navigate(Screen.DiaryHistory.route) },
                    onNavigateToActivity = { type -> navController.navigate(Screen.ActivityDetail.createRoute(type)) },
                    onNavigateToBreathing = { navController.navigate(Screen.BreathingExercise.route) },
                    onNavigateToPanic = { /* Logic Call */ }
                )
            }

            composable(Screen.PsychologistMap.route) { PsychologistMapScreen() }
            composable(Screen.DepressionDetection.route) { DepressionClassifierScreen() }
            composable(Screen.Articles.route) { ArticleScreen() }

            // --- 2. PROFILE FLOW (UPDATED) ---
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToEdit = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToPassword = { navController.navigate(Screen.ChangePassword.route) },
                    onNavigateToAbout = { navController.navigate(Screen.AboutApp.route) },
                    onNavigateToTnc = { navController.navigate(Screen.TermsConditions.route) },
                    onLogout = {
                        // Navigasi balik ke Login dan hapus stack
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // --- 3. PROFILE SUB-SCREENS ---
            composable(Screen.EditProfile.route) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.AboutApp.route) {
                AboutAppScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.TermsConditions.route) {
                TermsConditionsScreen(onBack = { navController.popBackStack() })
            }

            // --- 4. ACTIVITY & DETAILS ---
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
                VideoPlayerScreen(
                    videoId = videoId,
                    title = title,
                    description = desc,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BreathingExercise.route) {
                BreathingScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.ExerciseGuide.route) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("exerciseId") ?: return@composable
                ExerciseGuideScreen(exerciseId = id, onBack = { navController.popBackStack() })
            }

            composable(Screen.DiaryHistory.route) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Full Diary History Page") }
            }
        }
    }
}

// Placeholder ArticleScreen (Jika belum ada filenya)
@Composable
fun ArticleScreen() { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Halaman Artikel") } }