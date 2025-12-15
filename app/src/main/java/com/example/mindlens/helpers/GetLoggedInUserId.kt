package com.example.mindlens.helpers

import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth

// helper func to get logged in user's ID
fun getLoggedInUserId(): String? {
    return DatabaseConnection.supabase.auth.currentUserOrNull()?.id
}