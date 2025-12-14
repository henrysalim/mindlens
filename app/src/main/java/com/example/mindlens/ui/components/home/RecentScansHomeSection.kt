package com.example.mindlens.ui.components.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.screens.depressionClassifier.ScanHistoryItem
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun RecentScansHomeSection(
    scans: List<ScanHistoryItem>,
    onScanClick: () -> Unit // Aksi saat klik "See All" atau card
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Scans",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TechTextPrimary
            )
            TextButton(onClick = onScanClick) {
                Text("See All", color = TechPrimary, fontWeight = FontWeight.Bold)
            }
        }

        // List Content
        if (scans.isEmpty()) {
            Text(
                "No recent scans found.",
                color = TechTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            scans.forEach { item ->
                ScanItemCard(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ScanItemCard(item: ScanHistoryItem) {
    val isRisk = item.result.contains("Depresi", ignoreCase = true) || item.result == "1"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.History, null, tint = TechTextSecondary)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.result,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isRisk) Color(0xFFEF5350) else Color(0xFF2E7D32)
                )
                Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
            }

            Text(
                "${item.confidencePercent.toInt()}%",
                fontWeight = FontWeight.Bold,
                color = TechPrimary
            )
        }
    }
}