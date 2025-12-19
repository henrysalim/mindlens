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

    private val _comments = MutableStateFlow<List<GetArticleComment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _replyingTo = MutableStateFlow<GetArticleComment?>(null)
    val replyingTo = _replyingTo.asStateFlow()

    fun setReplyingTo(comment: GetArticleComment?) {
        _replyingTo.value = comment
    }

    fun loadComments(newsUrl: String) {
        viewModelScope.launch {
            // Jangan set loading true jika ini hanya refresh diam-diam (opsional)
            // Tapi untuk awal, biarkan true agar user tahu sedang memuat
            _isLoading.value = true
            try {
                val rawList = repository.getComments(newsUrl)
                // Menggunakan logic baru yang lebih aman untuk Compose
                _comments.value = organizeComments(rawList)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun organizeComments(rawList: List<GetArticleComment>): List<GetArticleComment> {
        val (parents, children) = rawList.partition { it.parentId == null }

        val childrenMap = children.groupBy { it.parentId }

        return parents
            .map { parent ->
                val myChildren = childrenMap[parent.id]
                    ?.sortedBy { it.createdAt }
                    ?: emptyList()
                parent.copy(replies = myChildren)
            }
            .sortedByDescending { it.createdAt }
    }

    fun sendComment(
        newsUrl: String,
        commentText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentUser = DatabaseConnection.supabase.auth.currentUserOrNull()?.id
                if (currentUser == null) {
                    onError("User not logged in")
                    _isLoading.value = false
                    return@launch
                }

                val timestamp = java.time.Instant.now().toString()
                val parentIdToUse = _replyingTo.value?.id

                val newComment = PostArticleComment(
                    id = UUID.randomUUID().toString(),
                    comment = commentText,
                    news_url = newsUrl,
                    user_id = currentUser,
                    parentId = parentIdToUse,
                    createdAt = timestamp
                )

                // 1. Post ke Supabase
                repository.postComment(newComment)

                // 2. Reset state reply
                _replyingTo.value = null

                // 3. Refresh data
                loadComments(newsUrl)

                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to post comment")
                _isLoading.value = false
            }
        }
    }
}