package com.example.projectwithcompose

import android.app.Application
import com.example.projectwithcompose.supabase.DatabaseConnection

class DiaryApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // Pass the application context so we can access SharedPreferences
        DatabaseConnection.initialize(this)
    }
}