package com.example.mindlens.screens.article

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mindlens.data.Article
import com.example.mindlens.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: ArticleViewModel = viewModel()
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mental Health News", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
        containerColor = TechBackground // Pastikan background scaffold benar
    ) { padding ->
        // GANTI STRUKTUR UTAMA
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 50.dp) // Padding internal list (biar scroll tembus FAB)
        ) {
            // ITEM 1: Search Bar (Masuk sebagai item list)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search articles...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        viewModel.onSearch()
                        focusManager.clearFocus()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechPrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }

            // ITEM 2: Sort Chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = viewModel.sortBy == "publishedAt",
                        onClick = { viewModel.onSortChange("publishedAt") },
                        label = { Text("Newest") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = TechPrimary
                        )
                    )
                    FilterChip(
                        selected = viewModel.sortBy == "relevancy",
                        onClick = { viewModel.onSortChange("relevancy") },
                        label = { Text("Relevant") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TechPrimary.copy(alpha = 0.2f),
                            selectedLabelColor = TechPrimary
                        )
                    )
                }
            }

            // ITEM 3: Loading / Error / List Artikel
            if (viewModel.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = TechPrimary)
                    }
                }
            } else if (viewModel.errorMessage != null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
                        Text(text = viewModel.errorMessage ?: "Error", color = Color.Red)
                    }
                }
            } else {
                items(viewModel.articles) { article ->
                    ArticleItem(article, onClick = { onArticleClick(article) })
                }
            }

            // ITEM 4: Pagination (Masuk sebagai footer list)
            item {
                if (viewModel.articles.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.onPrevPage() },
                            enabled = viewModel.currentPage > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TechPrimary),
                            elevation = ButtonDefaults.buttonElevation(2.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TechPrimary)
                        ) {
                            Icon(Icons.Default.ChevronLeft, null)
                            Text("Prev")
                        }

                        Text("Page ${viewModel.currentPage}", fontWeight = FontWeight.Bold, color = TechTextPrimary)

                        Button(
                            onClick = { viewModel.onNextPage() },
                            enabled = viewModel.articles.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White)
                        ) {
                            Text("Next")
                            Icon(Icons.Default.ChevronRight, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleItem(article: Article, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            // 2. PERBAIKAN GAMBAR:
            // Menggunakan ImageRequest Builder agar lebih stabil
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(article.image) // URL dari API
                    .listener(
                        onStart = { Log.d("DEBUG_IMAGE", "Mulai loading: ${article.image}") },
                        onSuccess = { _, _ -> Log.d("DEBUG_IMAGE", "Sukses load gambar!") },
                        onError = { _, result -> Log.e("DEBUG_IMAGE", "Error load gambar: ${result.throwable.message}") }
                    )
                    .crossfade(true) // Efek muncul halus
                    .build(),
                contentDescription = "Article Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text Info
            Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.height(100.dp)) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TechTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Badge Sumber Berita
                    Surface(
                        color = TechPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = article.source?.name ?: "News",
                            style = MaterialTheme.typography.labelSmall,
                            color = TechPrimary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Tanggal
                    Text(
                        text = if (article.publishedAt.length >= 10) article.publishedAt.take(10) else article.publishedAt,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechTextSecondary
                    )
                }
            }
        }
    }
}