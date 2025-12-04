package com.example.mindlens.navigations

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {

    // --- MENU UTAMA (BOTTOM BAR) ---
    object Home : Screen("home_screen", "Home", Icons.Outlined.Home)
    object PsychologistMap : Screen("psychologist_map_screen", "Psikolog", Icons.Outlined.LocationOn)
    object DepressionDetection : Screen("depression_detection_screen", "Scan", Icons.Outlined.CameraAlt)
    object Articles : Screen("articles_screen", "Artikel", Icons.Outlined.Article)
    object ArticleDetail : Screen("article_detail", "Detail", Icons.Outlined.Description)
    object Profile : Screen("profile_screen", "Profile", Icons.Outlined.Person)

    // --- SUB-SCREENS (DETAIL) ---
    object DiaryHistory : Screen("diary_history", "History", Icons.Outlined.Book)

    // 1. List Aktivitas (Daftar Yoga/Meditasi)
    object ActivityDetail : Screen("activity_detail/{type}", "Activity", Icons.Outlined.FitnessCenter) {
        fun createRoute(type: String) = "activity_detail/$type"
    }

    // 2. Layar Video Player
    object VideoPlayer : Screen("video_player/{videoId}/{title}/{desc}", "Video", Icons.Outlined.PlayArrow) {
        fun createRoute(videoId: String, title: String, desc: String) = "video_player/$videoId/$title/$desc"
    }

    // 3. Layar Panduan Step-by-Step (INI YANG HILANG SEBELUMNYA)
    object ExerciseGuide : Screen("exercise_guide/{exerciseId}", "Guide", Icons.Outlined.Description) {
        fun createRoute(exerciseId: String) = "exercise_guide/$exerciseId"
    }

    // 4. Fitur Breathing
    object BreathingExercise : Screen("breathing_exercise", "Breathe", Icons.Outlined.Air)

    // --- PROFILE SUB-MENU ---
    object EditProfile : Screen("edit_profile", "Edit Profile", Icons.Outlined.Edit)
    object ChangePassword : Screen("change_password", "Change Password", Icons.Outlined.Lock)
    object AboutApp : Screen("about_app", "About", Icons.Outlined.Info)
    object TermsConditions : Screen("terms_conditions", "T&C", Icons.Outlined.Description)

    // Auth & Splash
    object Splash : Screen("splash_screen", "Splash", Icons.Outlined.Info)
    object Onboarding : Screen("onboarding_screen", "Onboarding", Icons.Outlined.Info)
    object Login : Screen("login_screen", "Login", Icons.Outlined.Login)
}