package com.example.projectwithcompose.supabase

import android.content.Context
import android.content.SharedPreferences
import com.example.projectwithcompose.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DatabaseConnection {
    lateinit var supabase: SupabaseClient

    fun initialize(context: Context) {
        supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                sessionManager = AndroidSessionManager(context)
            }

            install(ComposeAuth) {
                googleNativeLogin(serverClientId = BuildConfig.WEB_GOOGLE_CLIENT_ID)
            }
        }
    }

    class AndroidSessionManager(context: Context) : SessionManager {
        private val prefs: SharedPreferences = context.getSharedPreferences("supabase_auth", Context.MODE_PRIVATE)

        override suspend fun saveSession(session: UserSession) {
            val jsonSession = Json.encodeToString(session)
            prefs.edit().putString("saved_session", jsonSession).apply()
        }

        // 2. Load: Convert JSON String -> Object
        override suspend fun loadSession(): UserSession? {
            val jsonSession = prefs.getString("saved_session", null) ?: return null
            return try {
                Json.decodeFromString<UserSession>(jsonSession)
            } catch (e: Exception) {
                // If data is corrupted, return null so user has to login again
                null
            }
        }

        // 3. Delete: Remove from disk
        override suspend fun deleteSession() {
            prefs.edit().remove("saved_session").apply()
        }
    }
}