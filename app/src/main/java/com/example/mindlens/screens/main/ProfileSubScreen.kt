package com.example.mindlens.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.ui.*

// ----------------- EDIT PROFILE SCREEN -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf("John Doe") }
    var email by remember { mutableStateOf("john.doe@example.com") }
    var bio by remember { mutableStateOf("I love mindfulness.") }

    Scaffold(
        topBar = { SimpleTopBar("Edit Profile", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(TechBackground)
                .padding(24.dp)
        ) {
            CustomTextField(label = "Full Name", value = name, onValueChange = { name = it })
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = "Email Address", value = email, onValueChange = { email = it })
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = "Bio", value = bio, onValueChange = { bio = it }, maxLines = 3)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onBack() /* TODO: Save Logic */ },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------- CHANGE PASSWORD SCREEN -----------------
@Composable
fun ChangePasswordScreen(onBack: () -> Unit) {
    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    Scaffold(
        topBar = { SimpleTopBar("Change Password", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().background(TechBackground).padding(24.dp)
        ) {
            CustomTextField(label = "Current Password", value = currentPass, onValueChange = { currentPass = it }, isPassword = true)
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = "New Password", value = newPass, onValueChange = { newPass = it }, isPassword = true)
            Spacer(modifier = Modifier.height(16.dp))
            CustomTextField(label = "Confirm Password", value = confirmPass, onValueChange = { confirmPass = it }, isPassword = true)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update Password", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------- ABOUT APP SCREEN -----------------
@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { SimpleTopBar("About MindLens", onBack) }
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
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("ML", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("MindLens", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
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

// ----------------- TERMS & CONDITIONS SCREEN -----------------
@Composable
fun TermsConditionsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { SimpleTopBar("Terms & Conditions", onBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(TechBackground)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Spacer(modifier = Modifier.height(16.dp))

            val dummyText = """
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

            Text(dummyText, color = TechTextSecondary, lineHeight = 24.sp, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- HELPER COMPONENTS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, color = TechTextPrimary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, "Back", tint = TechTextPrimary)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
    )
}

@Composable
fun CustomTextField(label: String, value: String, onValueChange: (String) -> Unit, isPassword: Boolean = false, maxLines: Int = 1) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = TechTextSecondary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TechPrimary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
            // Tambahkan visual transformation untuk password jika perlu
        )
    }
}