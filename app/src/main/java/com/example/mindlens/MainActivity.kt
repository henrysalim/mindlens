package com.example.mindlens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.mindlens.ui.DailyDiaryTheme
import com.example.mindlens.ui.components.mainActivity.AppNavigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyDiaryTheme(dynamicColor = false) {
                AppNavigation()
            }
        }
    }
}