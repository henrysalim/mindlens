package com.example.mindlens.data

import com.example.mindlens.model.Article

data class NewsApiResponse(
    val totalArticles: Int,
    val articles: List<Article>
)