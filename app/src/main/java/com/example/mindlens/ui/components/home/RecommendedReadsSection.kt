package com.example.mindlens.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.screens.home.ArticleItem
import com.example.mindlens.ui.TechAccent
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun RecommendedReadsSection() {
    val articles = listOf(
        ArticleItem("Understanding Anxiety", "Education", "5 min"),
        ArticleItem("The Power of Sleep", "Health", "3 min"),
        ArticleItem("Journaling 101", "Productivity", "7 min")
    )
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text("Recommended for You", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
        Spacer(modifier = Modifier.height(12.dp))
        articles.forEach { article ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TechSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(TechPrimary.copy(0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Article, null, tint = TechPrimary)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(article.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TechTextPrimary)
                        Row { Text(article.category, fontSize = 10.sp, color = TechAccent); Text(" • ${article.readTime}", fontSize = 10.sp, color = TechTextSecondary) }
                    }
                }
            }
        }
    }
}
