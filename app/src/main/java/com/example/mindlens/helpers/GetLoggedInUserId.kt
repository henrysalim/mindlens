package com.example.mindlens.helpers

import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth

fun getLoggedInUserId(): String? {
    return DatabaseConnection.supabase.auth.currentUserOrNull()?.id
}