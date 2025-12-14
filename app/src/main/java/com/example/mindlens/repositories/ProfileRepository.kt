package com.example.mindlens.repositories

import android.util.Log
import com.example.mindlens.helpers.getLoggedInUserId
import com.example.mindlens.model.UserProfile
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class ProfileRepository {
    // get user's profile
    suspend fun getProfile(): UserProfile? {
        val userId = DatabaseConnection.supabase.auth.currentUserOrNull()?.id ?: return null

        return try {
            DatabaseConnection.supabase.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // update the user's profile
    suspend fun updateProfile(name: String, bio: String, base64Image: String?) {
        val userId = getLoggedInUserId() ?: return

        val updateData = mutableMapOf(
            "full_name" to name,
            "bio" to bio
        )

        // If the user picked a new image, save it to the 'avatar' column
        if (base64Image != null) {
            updateData["avatar"] = base64Image
        }

        try {
            DatabaseConnection.supabase.from("profiles")
                .update(updateData) {
                    filter { eq("id", userId) }
                }
        } catch (e: Exception) {
            Log.e("UPDATE ERROR", e.message.toString())
        }
    }
}