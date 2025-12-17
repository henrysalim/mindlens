package com.example.mindlens.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary

@Composable
fun MoodCheckInSection(onMoodSelected: (String, Color) -> Unit) {
    val colorAwful = Color(0xFFE57373)
    val colorBad = Color(0xFFFFB74D)
    val colorNeutral = Color(0xFFFFF176)
    val colorGood = Color(0xFFAED581)
    val colorGreat = Color(0xFF64B5F6)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).offset(y = (-75).dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = TechSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("How are you feeling?", fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MoodButton(Icons.Outlined.SentimentVeryDissatisfied, colorAwful, "Awful") { onMoodSelected("Awful", colorAwful) }
                MoodButton(Icons.Outlined.SentimentDissatisfied, colorBad, "Bad") { onMoodSelected("Bad", colorBad) }
                MoodButton(Icons.Outlined.SentimentNeutral, colorNeutral, "Okay") { onMoodSelected("Okay", colorNeutral) }
                MoodButton(Icons.Outlined.SentimentSatisfied, colorGood, "Good") { onMoodSelected("Good", colorGood) }
                MoodButton(Icons.Outlined.SentimentVerySatisfied, colorGreat, "Great") { onMoodSelected("Great", colorGreat) }
            }
        }
    }
}
