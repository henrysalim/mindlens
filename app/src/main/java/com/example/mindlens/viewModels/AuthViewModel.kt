package com.example.mindlens.viewModels

import android.util.Log
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

    // Check if user is already logged in (For Splash Screen)
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
            // Calls the SQL function to check whether the email is exist/not
            val result = DatabaseConnection.supabase.postgrest.rpc(
                function = "check_email_exists",
                parameters = mapOf("email_to_check" to email)
            ).decodeAs<Boolean>()

            return result
        } catch (e: Exception) {
            Log.e("ERR_REG_EMAIL", e.message.toString())
            false
        }
    }

    // The signUp function
    fun signUp(
        email: String,
        pass: String,
        fullName: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val emailExists = isEmailRegistered(email)
            // check if email exist/not
            if (emailExists) {
                _authState.value = AuthState.Error("Email already taken")
                onError("This email is already registered. Please login.")
                return@launch
            }

            try {
                DatabaseConnection.supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }

                // Standard success logic...
                val session = DatabaseConnection.supabase.auth.currentSessionOrNull()
                if (session != null) {
                    onSuccess()
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                onError(e.message ?: "Sign up failed")
                _authState.value = AuthState.Error(e.message ?: "Error")
            }
        }
    }

    // Sign In with Email
    fun signIn(
        email: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // Attempt to Login
                DatabaseConnection.supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }

                // If code reaches here, it means credentials are CORRECT!
                _authState.value = AuthState.Authenticated
                onSuccess()

            } catch (e: Exception) {
                // If code jumps here, credentials are WRONG (or network issue)
                // Common message: "Invalid login credentials"
                val errorMessage = e.message ?: "Login failed"
                Log.e("ERR_SIGN_IN_EMAIL", errorMessage)

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
                _authState.value = AuthState.Authenticated
            }

            is NativeSignInResult.Error -> {
                _authState.value = AuthState.Error(result.message)
            }

            is NativeSignInResult.NetworkError -> {
                _authState.value = AuthState.Error("Network error: ${result.message}")
            }

            is NativeSignInResult.ClosedByUser -> {
            }
        }
    }

    // Sign Out
    fun signOut() {
        viewModelScope.launch {
            DatabaseConnection.supabase.auth.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun getUserName(): String {
        val user = DatabaseConnection.supabase.auth.currentUserOrNull()

        // get the user's full name
        val fullName = user?.userMetadata?.get("full_name")?.jsonPrimitive?.content
            ?: user?.userMetadata?.get("name")?.jsonPrimitive?.content
            ?: user?.userMetadata?.get("Display name")?.jsonPrimitive?.content // Jaga-jaga kalau ada data lama
            ?: "User"

        return fullName
    }
}

// Simple State Management
sealed class AuthState {
    object Loading : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}