package com.example.projectwithcompose.navigations

import RegisterOptionsScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.screens.auth.EmailRegisterScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "register_options") {

        // 1. The Social Login Screen (From your previous screenshot)
        composable("register_options") {
            RegisterOptionsScreen(
                onEmailClick = { navController.navigate("register_email") }
            )
        }

        // 2. The New Email Form Screen (Your new screenshot)
        composable("register_email") {
            EmailRegisterScreen(
                onLoginClick = {
                    navController.navigate("login")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 3. The Login Screen (Placeholder)
        composable("login") {
            // Replace this with your actual Login Screen later
            RegisterOptionsScreen(
                onEmailClick = {
                    navController.navigate("register_email")
                }
            )
        }
    }
}