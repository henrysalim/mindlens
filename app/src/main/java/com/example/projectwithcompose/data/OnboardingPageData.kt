package com.example.projectwithcompose.data

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.projectwithcompose.R // MAKE SURE TO IMPORT YOUR R FILE

// 1. Define the Accent Color from the design
val DiaryGreen = Color(0xFF8BC34A) // A close approximation based on the image

// 2. Data class for a single onboarding page structure
data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

// 3. The actual content list
val onboardingPagesList = listOf(
    OnboardingPageData(
        imageRes = R.drawable.ic_illus_onboarding_1, // Replace with your actual resource IDs
        title = "Diary with lock",
        description = "Store your memories in a safe and secure way!"
    ),
    OnboardingPageData(
        imageRes = R.drawable.ic_illus_onboarding_2,
        title = "Mood graph",
        description = "We can show which part of the year kept you happy, sad or angry with cool graphs!"
    ),
    OnboardingPageData(
        imageRes = R.drawable.ic_illus_onboarding_3,
        title = "Search diary",
        description = "Effortlessly search your diary to relive a particular memory!"
    ),
    OnboardingPageData(
        imageRes = R.drawable.ic_illus_onboarding_4,
        title = "Most secure",
        description = "Save memories with PIN, Face lock and intruder selfie"
    )
)

// A helper for standard text styles (optional, but helps match design)
@Composable
fun OnboardingTitleText(text: String) {
    androidx.compose.material3.Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )
}

@Composable
fun OnboardingBodyText(text: String) {
    androidx.compose.material3.Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.Gray,
        lineHeight = 24.sp
    )
}