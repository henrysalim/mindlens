package com.example.projectwithcompose.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- 1. Definisi Warna (Sesuai Figma) ---
// Kalau sudah ada di Color.kt, bagian ini bisa dihapus dan di-import saja
val PrimaryGreen = Color(0xFFC6F432) // Hijau Neon
val BackgroundWhite = Color(0xFFF8F9FA) // Abu sangat muda
val SurfaceWhite = Color(0xFFFFFFFF) // Putih murni
val TextBlack = Color(0xFF1A1A1A) // Hitam pekat
val TextGray = Color(0xFF9E9E9E)

// --- 2. Skema Warna Light (Fokus Utama Desain) ---
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

// --- 3. Skema Warna Dark (Opsional/Adaptasi) ---
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreen, // Tetap hijau agar kontras
    onPrimary = TextBlack,

    background = Color(0xFF121212), // Hitam gelap
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

@Composable
fun DailyDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // UBAH KE FALSE: Agar warna aplikasi konsisten sesuai Figma (Hijau/Hitam)
    // Jika true, warna akan mengikuti wallpaper HP user (biasanya pastel/coklat/biru)
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Ubah warna Status Bar agar menyatu dengan background (Putih/Abu)
            // Bukan primary (Hijau), biar terlihat clean.
            window.statusBarColor = colorScheme.background.toArgb()

            // Mengatur ikon status bar (jam, baterai) jadi gelap jika background terang
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}