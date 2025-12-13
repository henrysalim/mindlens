package com.example.mindlens.ui.components.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun RecentHistorySection(diaryList: List<DiaryEntry>, onSeeAllClick: () -> Unit, onItemClick: (DiaryEntry) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recent Diaries", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TechTextPrimary)
            Text("See All", color = TechPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { onSeeAllClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (diaryList.isEmpty()) {
            Text("No diaries yet. Add one above!", color = TechTextSecondary, fontSize = 12.sp)
        } else {
            diaryList.take(3).forEach { entry ->
                DiaryItem(entry, onClick = { onItemClick(entry) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
