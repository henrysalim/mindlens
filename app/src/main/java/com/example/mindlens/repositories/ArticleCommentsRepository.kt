package com.example.mindlens.repositories

import android.util.Log
import com.example.mindlens.model.GetArticleComment
import com.example.mindlens.model.PostArticleComment
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class ArticleCommentsRepository {
    private val supabase = DatabaseConnection.supabase
    suspend fun postComment(comment: PostArticleComment) {
        // Insert the comment into the 'article_comments' table
        try {
            supabase.from("article_comments").insert(comment)
        } catch (e: Exception) {
            // log the error if occurs
            Log.e("ERR_POST_COMMENT", e.message.toString())
        }
    }

    // Fetch comments for a specific article
    suspend fun getComments(newsUrl: String): List<GetArticleComment> {
        try {
            val result = supabase
                .from("article_comments")
                .select(
                    columns = Columns.list(
                        "id", "comment", "user_id", "created_at", "news_url", "parent_id", "profiles(full_name, avatar)"
                    )
                ) { filter { eq("news_url", newsUrl) } } // Ambil semua komen (parent & child)

            return result.decodeList<GetArticleComment>()
        } catch (e: Exception) {
            Log.e("ERROR_JOIN", "Error: ${e.message}")
            return emptyList()
        }
    }
}