package com.example.mindlens.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun CustomSuccessToast(
    visible: Boolean,
    message: String = "Saved Successfully!",
    onDismiss: () -> Unit
) {
    // Auto-hide after 2 seconds
    LaunchedEffect(visible) {
        if (visible) {
            delay(2000)
            onDismiss()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 50.dp), // Position from top
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { -40 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -40 }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xFF4CAF50), RoundedCornerShape(24.dp)) // Green bg
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = message, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}