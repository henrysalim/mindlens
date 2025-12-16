package com.example.mindlens.ui.components.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.dataClass.WeeklyData
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun WeeklyChartSection(
    weeklyData: List<WeeklyData>,
    averageMoodStatus: String
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp).offset(y = (-30).dp)) {
        Text("Weekly Analytics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Average over last 7 days:", style = MaterialTheme.typography.labelMedium, color = TechTextSecondary)
        Spacer(modifier = Modifier.height(10.dp))
        Text(averageMoodStatus, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TechPrimary)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = TechSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth().height(180.dp)
        ) {
            if (weeklyData.all { it.score == 0f }) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("You don't have any activities this week", color = TechTextSecondary)
                }
            } else {
                Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp)) {
                        val width = size.width
                        val height = size.height
                        val path = Path()
                        val stepX = width / (weeklyData.size - 1).coerceAtLeast(1)

                        weeklyData.forEachIndexed { index, data ->
                            val x = index * stepX
                            val y = height - (data.score * height * 0.8f)
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            if (data.score > 0) {
                                drawCircle(color = data.color, radius = 12f, center = Offset(x, y))
                                drawCircle(color = Color.White, radius = 6f, center = Offset(x, y))
                            }
                        }
                        drawPath(path = path, color = TechPrimary.copy(alpha = 0.5f), style = Stroke(width = 5f, cap = StrokeCap.Round))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weeklyData.forEach {
                            Text(it.day, fontSize = 10.sp, color = if (it.score > 0) TechTextPrimary else TechTextSecondary, fontWeight = if (it.score > 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}
