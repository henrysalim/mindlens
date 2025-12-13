package com.example.mindlens.repositories

import com.example.mindlens.model.ArticleComments
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class ArticleCommentsRepository {
    suspend fun postComment(comment: ArticleComments) {
        // Insert the comment into the 'article_comments' table
        DatabaseConnection.supabase.from("article_comments").insert(comment)
    }

    // Optional: Function to fetch comments for a specific article
    suspend fun getComments(newsUrl: String): List<ArticleComments> {
        return DatabaseConnection.supabase
            .from("article_comments")
            .select {
                filter {
                    eq("news_url", newsUrl)
                }
                // Order by newest first
                order("created_at", order = Order.DESCENDING)
            }
            .decodeList<ArticleComments>()
    }
}