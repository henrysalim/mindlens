package com.example.mindlens.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.jsonPrimitive

class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()

    // 1. Check if user is already logged in (For Splash Screen)
    fun checkAuthStatus() {
        viewModelScope.launch {
            // This checks the local storage (SharedPreferences) for a valid token
            val session = DatabaseConnection.supabase.auth.currentSessionOrNull()

            if (session != null) {
                _authState.value = AuthState.Authenticated
            } else {
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    // 2. Sign Up with Email
    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                DatabaseConnection.supabase.auth.signUpWith(Email) {
                    this.email = email
                    password = pass
                }
                // Note: Depending on Supabase settings, they might need to verify email first.
                // If email confirmation is OFF, they are logged in.
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Sign up failed")
            }
        }
    }

    // 3. Sign In with Email
    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                DatabaseConnection.supabase.auth.signInWith(Email) {
                    this.email = email
                    password = pass
                }
                _authState.value = AuthState.Authenticated
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun handleGoogleResult(result: NativeSignInResult) {
        when (result) {
            is NativeSignInResult.Success -> {
                // Supabase Client has already handled the token exchange internally.
                // We just need to update our UI state.
                _authState.value = AuthState.Authenticated
            }
            is NativeSignInResult.Error -> {
                _authState.value = AuthState.Error(result.message)
            }
            is NativeSignInResult.NetworkError -> {
                _authState.value = AuthState.Error("Network error: ${result.message}")
            }
            is NativeSignInResult.ClosedByUser -> {
                // Usually we don't change state here, just stay on the login screen
                // Or you could set a specialized error if you want to show a snackbar
            }
        }
    }

    // 5. Sign Out
    fun signOut() {
        viewModelScope.launch {
            DatabaseConnection.supabase.auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getUserName(): String {
        val user = DatabaseConnection.supabase.auth.currentUserOrNull()

        // Google stores the name inside the "full_name" or "name" key in metadata
        val fullName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.content
            ?: user?.userMetadata?.get("name")?.jsonPrimitive?.content
            ?: "User" // Fallback if no name found

        return fullName
    }

    fun getEmail(): String {
        val user = DatabaseConnection.supabase.auth.currentUserOrNull()

        // The email is a top-level property of the User object
        return user?.email ?: "No Email"
    }

    fun getUserAvatar(): String? {
        val user = DatabaseConnection.supabase.auth.currentUserOrNull()
        return user?.userMetadata?.get("avatar_url")?.jsonPrimitive?.content
    }
}

// Simple State Management
sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}