package com.example.mindlens.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.example.mindlens.BuildConfig

object DatabaseConnection {
    // define supabase variable
    lateinit var supabase: SupabaseClient

    // initialize connection
    fun initialize(context: Context) {
        supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            // for session management
            install(Auth) {
                sessionManager = AndroidSessionManager(context)
            }

            // for Google Login
            install(ComposeAuth) {
                googleNativeLogin(serverClientId = BuildConfig.WEB_GOOGLE_CLIENT_ID)
            }

            // Postgre serializer
            install(Postgrest) {
                serializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true // ignores extra fields in DB
                    encodeDefaults = true    // sends default values (like nulls) correctly
                })
            }
        }
    }

    // special class to manage session (saving and loading)
    class AndroidSessionManager(context: Context) : SessionManager {
        private val prefs: SharedPreferences = context.getSharedPreferences("supabase_auth", Context.MODE_PRIVATE)

        // save session
        override suspend fun saveSession(session: UserSession) {
            val jsonSession = Json.encodeToString(session)
            prefs.edit().putString("saved_session", jsonSession).apply()
        }

        // Convert JSON String to Object
        override suspend fun loadSession(): UserSession? {
            val jsonSession = prefs.getString("saved_session", null) ?: return null
            return try {
                Json.decodeFromString<UserSession>(jsonSession)
            } catch (e: Exception) {
                Log.e("ERR_LOAD_SESSION", e.message.toString())
                // If data is corrupted, return null so user has to login again
                null
            }
        }

        // Remove session from disk
        override suspend fun deleteSession() {
            prefs.edit().remove("saved_session").apply()
        }
    }
}