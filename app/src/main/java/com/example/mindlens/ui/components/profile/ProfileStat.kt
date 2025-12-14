package com.example.mindlens.ui.components.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechPrimary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TechTextSecondary)
    }
}