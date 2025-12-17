package com.example.mindlens.screens.profile.subprofile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary
import com.example.mindlens.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            // the top bar
            TopAppBar(
                title = { Text("About MindLens", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
        containerColor = TechBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(TechBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Placeholder
            Card(
                modifier = Modifier.size(100.dp),
                colors = CardDefaults.cardColors(containerColor = TechPrimary),
                shape = RoundedCornerShape(24.dp)
            ) {
                // MindLens logo
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "MindLens Logo"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "MindLens",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TechTextPrimary
            )
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = TechTextSecondary)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "MindLens is an AI-powered mental health companion designed to help you track your mood, journal your thoughts, and detect early signs of depression using facial analysis.",
                textAlign = TextAlign.Center,
                color = TechTextSecondary,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text("© 2025 MindLens Team", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
