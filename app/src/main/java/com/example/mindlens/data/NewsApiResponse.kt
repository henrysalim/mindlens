package com.example.mindlens.data

import com.example.mindlens.model.Article

// For storing news response
data class NewsApiResponse(
    val totalArticles: Int,
    val articles: List<Article>
)