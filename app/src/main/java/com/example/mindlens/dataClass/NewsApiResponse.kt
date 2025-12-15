package com.example.mindlens.dataClass

import com.example.mindlens.model.Article

// Data structure for storing news response
data class NewsApiResponse(
    val totalArticles: Int,
    val articles: List<Article>
)