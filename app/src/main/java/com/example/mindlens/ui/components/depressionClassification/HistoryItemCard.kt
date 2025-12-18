package com.example.mindlens.ui.components.depressionClassification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.dataClass.ScanHistoryItem
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun HistoryItemCard(
    item: ScanHistoryItem,
    onDeleteClick: (String) -> Unit
) {
    val isRisk = item.result.contains("Depressed", ignoreCase = true) || item.result == "1"

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
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

            Text("${item.confidencePercent.toInt()}%", fontWeight = FontWeight.Bold, color = TechPrimary)

            IconButton(onClick = { onDeleteClick(item.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
        }
    }
}