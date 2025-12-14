package com.example.mindlens.navigations

import android.net.Uri

sealed class Screen(val route: String) {
    // Bottom Bar Tabs
    object Home : Screen("home")
    object PsychologistMap : Screen("psychologist_map")
    object DepressionDetection : Screen("depression_detection")
    object Articles : Screen("articles")
    object Profile : Screen("profile")

    // Sub-Screens
    object ArticleDetail : Screen("article_detail")

    // Activity & Video
    object ActivityDetail : Screen("activity_detail/{type}") {
        fun createRoute(type: String) = "activity_detail/$type"
    }
    object VideoPlayer : Screen("video_player/{videoId}/{title}/{desc}") {
        fun createRoute(videoId: String, title: String, desc: String) =
            "video_player/$videoId/${Uri.encode(title)}/${Uri.encode(desc)}"
    }
    object BreathingExercise : Screen("breathing_exercise")
    object ExerciseGuide : Screen("exercise_guide/{exerciseId}")

    // Diary Features
    object DiaryHistory : Screen("diary_history") // Rute untuk See All
    object DiaryDetail : Screen("diary_detail/{entry}") { // Rute untuk Detail
        fun createRoute(encodedJson: String) = "diary_detail/$encodedJson"
    }

    // Profile Settings
    object EditProfile : Screen("edit_profile")
    object ChangePassword : Screen("change_password")
    object AboutApp : Screen("about_app")
    object TermsConditions : Screen("terms_conditions")
}