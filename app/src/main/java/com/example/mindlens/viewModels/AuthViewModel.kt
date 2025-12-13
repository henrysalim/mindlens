package com.example.mindlens.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlens.supabase.DatabaseConnection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

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

    suspend fun isEmailRegistered(email: String): Boolean {
        return try {
            // Calls the SQL function we just created
            val result = DatabaseConnection.supabase.postgrest.rpc(
                function = "check_email_exists",
                parameters = mapOf("email_to_check" to email)
            ).decodeAs<Boolean>()

            return result
        } catch (e: Exception) {
            false // Default to false if check fails (let standard signup handle it)
        }
    }

    // 2. Update the signUp function
    fun signUp(
        email: String,
        pass: String,
        fullName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // --- STEP A: Check if email is taken ---
            val emailExists = isEmailRegistered(email)

            if (emailExists) {
                _authState.value = AuthState.Error("Email already taken")
                onError("This email is already registered. Please login.")
                return@launch // STOP HERE
            }

            // --- STEP B: Proceed with Signup ---
            try {
                DatabaseConnection.supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    data = buildJsonObject {
                        // GANTI "Display name" MENJADI "full_name" (Standar Supabase)
                        put("full_name", fullName)
                    }
                }

                // Standard success logic...
                val session = DatabaseConnection.supabase.auth.currentSessionOrNull()
                if (session != null) {
                    onSuccess()
                } else {
                    onSuccess() // Or show "Confirm Email" message
                }
            } catch (e: Exception) {
                onError(e.message ?: "Sign up failed")
                _authState.value = AuthState.Error(e.message ?: "Error")
            }
        }
    }

    // 3. Sign In with Email
    fun signIn(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Attempt to Login
                DatabaseConnection.supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }

                // 2. If code reaches here, it means credentials are CORRECT!
                _authState.value = AuthState.Authenticated
                onSuccess()

            } catch (e: Exception) {
                // 3. If code jumps here, credentials are WRONG (or network issue)
                // Common message: "Invalid login credentials"
                val errorMessage = e.message ?: "Login failed"

                // Determine user-friendly message
                val cleanMessage = if (errorMessage.contains("Invalid login credentials")) {
                    "Wrong email or password."
                } else {
                    errorMessage
                }

                _authState.value = AuthState.Error(cleanMessage)
                onError(cleanMessage)
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

        // Cek berbagai kemungkinan key
        val fullName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.content
            ?: user?.userMetadata?.get("name")?.jsonPrimitive?.content
            ?: user?.userMetadata?.get("Display name")?.jsonPrimitive?.content // Jaga-jaga kalau ada data lama
            ?: "User"

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