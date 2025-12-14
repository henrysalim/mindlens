package com.example.mindlens.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.data.HomeScanItem
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun RecentScansHomeSection(scans: List<HomeScanItem>, onScanClick: () -> Unit = {}) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Health Scans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Text("Scan Now", color = TechPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onScanClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        scans.forEach { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onScanClick() }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (item.isRisk) Icons.Default.Warning else Icons.Default.CheckCircle, null, tint = if (item.isRisk) Color(0xFFEF5350) else Color(0xFF66BB6A), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.result, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Text(item.date, fontSize = 12.sp, color = TechTextSecondary)
                    }
                    Text("${item.confidence}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TechPrimary)
                }
            }
        }
    }
}