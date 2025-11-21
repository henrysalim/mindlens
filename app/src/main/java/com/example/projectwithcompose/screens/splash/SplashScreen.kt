package com.example.projectwithcompose.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.delay
import com.example.projectwithcompose.R

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // This effect runs once when the composable enters the screen
    LaunchedEffect(key1 = true) {
        delay(2000) // Wait for 2 seconds
        onSplashFinished() // Trigger navigation
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Replace with your logo drawable
//        Image(
//            painter = painterResource(id = R.drawable.ic_diary_logo),
//            contentDescription = "Logo"
//        )
        Text(text = "MindLens", fontWeight = FontWeight.Bold)
    }
}