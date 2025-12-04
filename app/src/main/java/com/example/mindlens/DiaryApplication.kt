package com.example.mindlens

import android.app.Application
import com.example.mindlens.supabase.DatabaseConnection

class DiaryApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Pass the application context so we can access SharedPreferences
        DatabaseConnection.initialize(this)
    }
}