package com.example.mindlens.ui

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val PrimaryGreen = Color(0xFFC6F432) // Hijau Neon
val BackgroundWhite = Color(0xFFF8F9FA) // Abu sangat muda
val SurfaceWhite = Color(0xFFFFFFFF) // Putih murni
val TextBlack = Color(0xFF1A1A1A) // Hitam pekat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    onPrimary = TextBlack, // Teks di atas tombol hijau warnanya hitam
    primaryContainer = PrimaryGreen,
    onPrimaryContainer = TextBlack,

    secondary = TextBlack,
    onSecondary = Color.White,

    background = BackgroundWhite,
    onBackground = TextBlack,

    surface = SurfaceWhite,
    onSurface = TextBlack,

    surfaceVariant = Color(0xFFEEEEEE), // Untuk card yang lebih gelap dikit
    onSurfaceVariant = TextBlack
)

@Composable
fun DailyDiaryTheme(
    darkTheme: Boolean = false, // agar tema selalu terang sekalipun user gunakan light theme mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()

            // Mengatur ikon status bar (jam, baterai) jadi gelap jika background terang
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}