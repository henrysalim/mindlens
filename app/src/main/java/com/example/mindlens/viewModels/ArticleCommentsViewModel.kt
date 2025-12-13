package com.example.mindlens.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.model.ArticleComments
import com.example.mindlens.repositories.ArticleCommentsRepository
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ArticleCommentsViewModel: ViewModel() {
    private val repository = ArticleCommentsRepository()

    // State for the list of comments
    private val _comments = MutableStateFlow<List<ArticleComments>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Fetch comments when the screen loads
    fun loadComments(newsUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = repository.getComments(newsUrl)
                _comments.value = result
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Post a new comment
    fun sendComment(
        newsUrl: String,
        commentText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // 1. Get current User ID (optional, but good practice if you have user_id column)
                val currentUser = DatabaseConnection.supabase.auth.currentUserOrNull()
                // If you added 'user_id' to your table, pass it here.
                // For now, we stick to your data class model.

                // 2. Create the object
                val newComment = ArticleComments(
                    // Generate a random ID locally or let DB handle it (if auto-gen)
                    // If your DB ID is UUID, generate one here:
                    id = UUID.randomUUID().toString(),
                    comment = commentText,
                    news_url = newsUrl,
                    createdAt = null // Let Supabase handle the timestamp
                )

                // 3. Send to Supabase
                repository.postComment(newComment)

                // 4. Refresh the list locally to show the new comment immediately
                loadComments(newsUrl)

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to post comment")
            }
        }
    }
}