package com.example.mindlens.viewModels

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.BuildConfig
import com.example.mindlens.model.Article
import com.example.mindlens.services.NewsApiService
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    private val apiService = NewsApiService.create()

    // The API key is limited (once the limit has reached, no article will be displayed)
    private val apiKey = BuildConfig.ARTICLE_API_KEY

    // UI states
    var articles by mutableStateOf<List<Article>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // keyword for auto search later
    var searchQuery by mutableStateOf("kesehatan mental")
    var currentPage by mutableIntStateOf(1)
    var sortBy by mutableStateOf("publishedAt")

    init {
        // fetch articles right after the screen has opened
        fetchArticles()
    }

    fun fetchArticles() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.searchArticles(
                    query = searchQuery,
                    page = currentPage,
                    sortBy = sortBy,
                    apiKey = apiKey
                )
                if (response.isSuccessful && response.body() != null) {
                    articles = response.body()!!.articles
                } else {
                    errorMessage = "Gagal: ${response.code()} - ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Koneksi Error: Cek internet Anda"
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
    }

    fun onSearch() {
        currentPage = 1
        fetchArticles()
    }

    fun onNextPage() {
        currentPage++
        fetchArticles()
    }

    fun onPrevPage() {
        if (currentPage > 1) {
            currentPage--
            fetchArticles()
        }
    }

    fun onSortChange(newSort: String) {
        sortBy = newSort
        currentPage = 1
        fetchArticles()
    }
}