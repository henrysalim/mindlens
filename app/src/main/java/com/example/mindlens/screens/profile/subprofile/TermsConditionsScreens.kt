package com.example.mindlens.screens.profile.subprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.ui.TechBackground
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(onBack: () -> Unit) {
    val termsConditions = """
                1. Introduction
                Welcome to MindLens. By using our app, you agree to these terms.

                2. Health Disclaimer
                MindLens is not a medical device. The AI analysis is for informational purposes only and does not constitute a medical diagnosis. Please consult a professional for medical advice.

                3. Data Privacy
                We value your privacy. Your facial data is processed on-device and is not uploaded to our servers unless explicitly opted-in for research purposes.

                4. User Content
                You retain ownership of your diary entries. MindLens stores this data locally on your device or securely in your private cloud storage.

                5. Changes to Terms
                We may update these terms from time to time. Continued use of the app implies acceptance of the new terms.
            """.trimIndent()

    Scaffold(
        topBar = {
            // the top bar
            TopAppBar(
                title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // the title
            Text(
                "Terms of Service",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TechTextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                termsConditions,
                color = TechTextSecondary,
                lineHeight = 24.sp,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}