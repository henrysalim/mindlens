package com.example.projectwithcompose.navigations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Person4
import androidx.compose.material.icons.rounded.Place
import androidx.compose.ui.graphics.vector.ImageVector

// Screen.kt
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Home)
    object Mood : Screen("mood", "Check-in", Icons.Rounded.Favorite)
    object CameraML : Screen("camera_ml", "Scan", Icons.Rounded.Person)
    object Map : Screen("map", "Find Help", Icons.Rounded.Place)
    object Profile : Screen("profile", "Profile", Icons.Rounded.Person4)
}
