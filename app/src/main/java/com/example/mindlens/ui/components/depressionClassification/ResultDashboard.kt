package com.example.mindlens.ui.components.depressionClassification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlens.dataClass.ScanHistoryItem
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun ResultDashboard(result: ScanHistoryItem) {
    val isPositive = result.result.contains("Depresi", ignoreCase = true) || result.result == "1"
    val color = if (isPositive) Color(0xFFEF5350) else Color(0xFF66BB6A)
    val icon = if (isPositive) Icons.Default.Warning else Icons.Default.CheckCircle

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                result.result,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "Confidence: ${"%.1f".format(result.confidencePercent)}%",
                style = MaterialTheme.typography.bodyMedium,
                color = TechTextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color.LightGray.copy(0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Text("Recommended Action:", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(8.dp))

            if (isPositive) {
                RecommendationItem(
                    Icons.Default.Call,
                    "Call Helpline",
                    "Talk to a professional now.",
                    Color(0xFFFFF3E0)
                )
                Spacer(modifier = Modifier.height(8.dp))
                RecommendationItem(
                    Icons.Default.SelfImprovement,
                    "Try Meditation",
                    "Calm your mind with guided audio.",
                    Color(0xFFE1F5FE)
                )
            } else {
                RecommendationItem(
                    Icons.Default.SentimentSatisfied,
                    "Keep it up!",
                    "Maintain your good mood.",
                    Color(0xFFE8F5E9)
                )
            }
        }
    }
}