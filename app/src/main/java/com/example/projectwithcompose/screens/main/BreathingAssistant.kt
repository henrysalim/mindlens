package com.example.projectwithcompose.screens.main

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectwithcompose.ui.TechPrimary

@Composable
fun BreathingScreen(onBack: () -> Unit) {
    var isBreathing by remember { mutableStateOf(false) }
    val instruction = if (isBreathing) "Inhale..." else "Tap Start"

    // Animasi Infinite
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isBreathing) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing), // 4 detik tarik napas
            repeatMode = RepeatMode.Reverse // 4 detik buang napas
        )
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFE0F7FA)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Mindful Breathing", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TechPrimary)
        Spacer(modifier = Modifier.height(50.dp))

        // Lingkaran Animasi
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(if (isBreathing) scale else 1f)
                .background(TechPrimary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(TechPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isBreathing) (if (scale > 1.25f) "Exhale" else "Inhale") else "Ready?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Button(
            onClick = { isBreathing = !isBreathing },
            colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
            modifier = Modifier.height(50.dp).width(200.dp)
        ) {
            Text(if (isBreathing) "Stop" else "Start Exercise")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
            Text("Back to Home")
        }
    }
}