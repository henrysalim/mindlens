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
import com.example.mindlens.viewModels.ArticleViewModel
import com.example.mindlens.model.Article
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.article.ArticleItem

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
        containerColor = TechBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 50.dp)
        ) {
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

