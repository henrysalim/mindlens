package com.example.mindlens.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.model.GetArticleComment
import com.example.mindlens.model.PostArticleComment
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
    private val _comments = MutableStateFlow<List<GetArticleComment>>(emptyList())
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
                val currentUser = DatabaseConnection.supabase.auth.currentUserOrNull()?.id

                val timestamp = java.time.Instant.now().toString()

                // Create the object
                val newComment = PostArticleComment(
                    id = UUID.randomUUID().toString(),
                    comment = commentText,
                    news_url = newsUrl,
                    user_id = currentUser,
                    createdAt = timestamp // Let Supabase handle the timestamp
                )

                // Send to Supabase
                repository.postComment(newComment)

                // Refresh the list locally to show the new comment immediately
                loadComments(newsUrl)

                onSuccess()
            } catch (e: Exception) {
                // if error occurs
                onError(e.message ?: "Failed to post comment")
            }
        }
    }
}