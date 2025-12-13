package com.example.mindlens.viewModels

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.repositories.ProfileRepository
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val repository = ProfileRepository()

    // State variables
    var name = mutableStateOf("") // Will be pre-filled
    var email = mutableStateOf("") // Will be pre-filled
    var bio = mutableStateOf("") // Will be pre-filled

    // Image States
    var currentAvatarBase64 = mutableStateOf<String?>(null)
    var googleAvatarUrl = mutableStateOf<String?>(null)

    var isLoading = mutableStateOf(true) // Start as true to show spinner immediately

    // 1. Pre-fill the form with current logged in user's data
    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val profile = repository.getProfile()
                if (profile != null) {
                    name.value = profile.fullName ?: ""
                    bio.value = profile.bio ?: ""
                    currentAvatarBase64.value = profile.avatar ?: ""
                    email.value = DatabaseConnection.supabase.auth.currentUserOrNull()?.email ?: ""
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading.value = false
            }
        }
    }

    // 2. Save the changes
    fun saveChanges(context: Context, newImageUri: Uri?, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val newBase64 = if (newImageUri != null) {
                    ImageUtils.uriToBase64(context, newImageUri)
                } else null

                repository.updateProfile(name.value, bio.value, newBase64)

                onSuccess()

            } catch (e: Exception) {
                android.util.Log.e("EditProfile", "Save failed", e)
                onError(e.message ?: "Unknown error")
            } finally {
                isLoading.value = false
            }
        }
    }
}