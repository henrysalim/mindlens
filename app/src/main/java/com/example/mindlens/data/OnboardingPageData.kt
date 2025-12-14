package com.example.mindlens.data

import com.example.mindlens.R

// Data class for a single onboarding page structure
data class OnboardingPageData(
    val imageRes: Int,
    val title: String,
    val description: String
)

// Actual content for boarding screens
val onboardingPagesList = listOf(
    OnboardingPageData(
        imageRes = R.drawable.ic_illus_onboarding_1,
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
)