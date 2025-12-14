package com.example.mindlens.repositories

import android.util.Log
import com.example.mindlens.model.GetArticleComment
import com.example.mindlens.model.PostArticleComment
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ArticleCommentsRepository {
    suspend fun postComment(comment: PostArticleComment) {
        // Insert the comment into the 'article_comments' table
        try {
            DatabaseConnection.supabase.from("article_comments").insert(comment)
        } catch (e: Exception) {
            Log.e("ERR_POST_COMMENT", e.message.toString())
        }
    }

    // Fetch comments for a specific article
    suspend fun getComments(newsUrl: String): List<GetArticleComment> {
        try {
            val result = DatabaseConnection.supabase
                .from("article_comments")
                .select(
                    columns = Columns.list(
                        "id", "comment", "user_id", "created_at", "news_url", "profiles(full_name, avatar)"
                    )
                ) { filter { eq("news_url", newsUrl) } }

            Log.d("DEBUG_JOIN", result.data)

            return result.decodeList<GetArticleComment>()
        } catch (e: Exception) {
            Log.e("ERROR_JOIN", "Error: ${e.message}")
            return emptyList()
        }
    }
}