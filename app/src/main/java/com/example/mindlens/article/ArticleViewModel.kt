package com.example.mindlens.screens.article

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.data.Article
import com.example.mindlens.data.NewsApiService
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    private val apiService = NewsApiService.create()

    // PENTING: Ganti dengan API Key GNews Anda yang Valid!
    // Jika kuota habis, artikel & gambar tidak akan muncul.
    private val apiKey = "0391aa4a8d865486adc2220023fcde74"

    // State UI
    var articles by mutableStateOf<List<Article>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    // 1. PERBAIKAN QUERY DEFAULT:
    // Query ini akan otomatis dijalankan saat halaman dibuka.
    // Menggunakan 'OR' agar mencari salah satu dari topik ini.
    var searchQuery by mutableStateOf("kesehatan mental")

    var currentPage by mutableIntStateOf(1)
    var sortBy by mutableStateOf("publishedAt") // Default: Terbaru

    init {
        // Fungsi ini otomatis jalan saat ViewModel dibuat (saat layar dibuka)
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
                    sortBy = sortBy, // Filter (Newest/Relevant) akan diterapkan di sini
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
        fetchArticles() // Refresh list dengan urutan baru
    }
}