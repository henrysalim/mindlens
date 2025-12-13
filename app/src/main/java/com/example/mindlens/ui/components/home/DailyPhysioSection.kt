package com.example.mindlens.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.screens.home.ActivityItem
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun DailyPhysioSection(onItemClick: (String) -> Unit) {
    val activities = listOf(
        ActivityItem("Meditation", "10 min", Icons.Rounded.SelfImprovement, Color(0xFFE1BEE7)),
        ActivityItem("Yoga", "15 min", Icons.Rounded.FitnessCenter, Color(0xFFB2DFDB)),
        ActivityItem("Breathing", "5 min", Icons.Rounded.Air, Color(0xFFBBDEFB))
    )

    Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-16).dp)) {
        Text("Mind & Body", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activities) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.width(120.dp).clickable { onItemClick(item.title) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(item.icon, contentDescription = null, tint = TechTextPrimary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Text(item.duration, fontSize = 12.sp, color = TechTextSecondary)
                    }
                }
            }
        }
    }
}
