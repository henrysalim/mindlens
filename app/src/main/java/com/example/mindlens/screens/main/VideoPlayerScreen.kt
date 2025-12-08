package com.example.mindlens.screens.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mindlens.ui.*
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    videoId: String,
    title: String,
    description: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State untuk mendeteksi error player
    var isPlayerError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Now Playing", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
        containerColor = TechBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TechBackground)
                .padding(padding)
        ) {
            // --- VIDEO AREA ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16 / 9f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayerError) {
                    // Tampilan Error Fallback
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Video tidak dapat diputar di sini.", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                // Buka langsung ke Aplikasi YouTube / Browser
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buka di YouTube App")
                        }
                    }
                } else {
                    // Player Asli
                    AndroidView(
                        factory = { ctx ->
                            YouTubePlayerView(ctx).apply {
                                lifecycleOwner.lifecycle.addObserver(this)
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        // Gunakan cueVideo (lebih ringan) daripada loadVideo
                                        youTubePlayer.cueVideo(videoId, 0f)
                                    }

                                    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                                        super.onError(youTubePlayer, error)
                                        // Jika error, ubah state UI
                                        isPlayerError = true
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // --- INFO ---
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodyLarge, color = TechTextSecondary)

                // Tambahan Tombol Alternatif (Selalu Muncul)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Tonton langsung di YouTube")
                }
            }
        }
    }
}