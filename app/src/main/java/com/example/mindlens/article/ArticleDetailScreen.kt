package com.example.mindlens.screens.article

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Pastikan pakai Coil yang sesuai (jika merah, coba ganti coil3 -> coil)
import coil.compose.AsyncImage
import com.example.mindlens.data.Article
// Hapus import ArticleComment yang lama karena kita buat di bawah
import com.example.mindlens.ui.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    article: Article,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // State untuk Komentar (Lokal Saja)
    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf<ArticleComment>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Article Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TechBackground)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // --- GAMBAR & JUDUL ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AsyncImage(
                        model = article.image ?: "https://via.placeholder.com/400",
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Source Badge
                    Surface(
                        color = TechPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = article.source?.name ?: "News",
                            color = TechPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TechTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = article.publishedAt.take(10), color = TechTextSecondary, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Deskripsi / Konten
                    Text(
                        text = article.description ?: "No description available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechTextPrimary,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.content ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Buka Browser
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Full Article in Browser")
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    Text("Comments (${comments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- LIST KOMENTAR ---
                items(comments) { comment ->
                    CommentItem(comment)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // Spacer bawah
            }

            // --- INPUT KOMENTAR (Di Bawah) ---
            Surface(
                shadowElevation = 16.dp,
                color = TechSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Write a comment...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TechPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                val date = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())
                                comments.add(ArticleComment("You", commentText, date))
                                commentText = ""
                            }
                        },
                        modifier = Modifier.background(TechPrimary, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: ArticleComment) {
    // FIX: Gunakan verticalAlignment
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (comment.user.isNotEmpty()) {
                Text(comment.user.first().toString(), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.user, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(comment.date, color = Color.Gray, fontSize = 12.sp)
            }
            Text(comment.text, fontSize = 14.sp, color = TechTextPrimary)
        }
    }
}

// --- DATA MODEL (Ditambahkan di sini agar tidak error Unresolved Reference) ---
data class ArticleComment(
    val user: String,
    val text: String,
    val date: String
)