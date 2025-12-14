package com.example.mindlens.screens.article

import android.content.Intent
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mindlens.model.Article
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.article.CommentItem
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.viewModels.ArticleCommentsViewModel
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class) // for using TopAppBar
@Composable
fun ArticleDetailScreen(
    article: Article,
    onBack: () -> Unit,
    viewModel: ArticleCommentsViewModel = viewModel()
) {
    // context
    val context = LocalContext.current

    // State for Input
    var commentText by remember { mutableStateOf("") }

    // Toast States
    var toastVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isToastError by remember { mutableStateOf(false) }

    // Subscribe to ViewModel Data
    val commentsList by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Load comments when this screen opens
    LaunchedEffect(article.url) {
        viewModel.loadComments(article.url)
    }

    // the view..
    Scaffold(
        topBar = {
            // top bar to display back button and page title
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
                // Image and title
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    AsyncImage(
                        model = article.image ?: "https://via.placeholder.com/400" /* if image from API is null*/,
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

                    // article title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TechTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // article published at date
                    Text(text = article.publishedAt.take(10), color = TechTextSecondary, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // divider
                    HorizontalDivider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Content
                    Text(
                        text = article.description ?: "No description available.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TechTextPrimary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // main article content
                    Text(
                        text = article.content ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TechTextSecondary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Open in browser
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, article.url.toUri())
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Read Full Article in Browser")
                    }

                    // Comments section
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Comments (${commentsList.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Comments list
                items(commentsList) { comment ->
                    // call CommentItem component to display each comment
                    CommentItem(comment)
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item { Spacer(modifier = Modifier.height(80.dp)) } // Spacer bawah
            }

            // Comment input
            Surface(
                shadowElevation = 16.dp,
                color = TechSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // comment input field
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

                    // Submit
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.sendComment(
                                    newsUrl = article.url,
                                    commentText = commentText,
                                    onSuccess = {
                                        // Success toast
                                        commentText = "" // Clear input
                                        toastMessage = "Comment posted!"
                                        isToastError = false
                                        toastVisible = true
                                    },
                                    onError = { error ->
                                        // Error toast
                                        toastMessage = error
                                        isToastError = true
                                        toastVisible = true
                                    }
                                )
                            }
                        },
                        modifier = Modifier.background(TechPrimary, CircleShape)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp).padding(4.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Send, null, tint = Color.White)
                        }
                    }
                }
            }
        }

        // to display toast after user submit comment
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .statusBarsPadding(), // Ensures it doesn't hide behind status bar
            contentAlignment = Alignment.TopCenter
        ) {
            CustomToast(
                visible = toastVisible,
                message = toastMessage,
                isError = isToastError,
                onDismiss = { toastVisible = false }
            )
        }
    }
}
