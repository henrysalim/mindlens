package com.example.mindlens.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun EditMoodSelector(
    currentMood: String,
    onMoodSelected: (String, Color) -> Unit
) {
    val moodList = listOf(
        Triple("Awful", Color(0xFFE57373), Icons.Outlined.SentimentVeryDissatisfied),
        Triple("Bad", Color(0xFFFFB74D), Icons.Outlined.SentimentDissatisfied),
        Triple("Okay", Color(0xFFFFF176), Icons.Outlined.SentimentNeutral),
        Triple("Good", Color(0xFFAED581), Icons.Outlined.SentimentSatisfied),
        Triple("Great", Color(0xFF64B5F6), Icons.Outlined.SentimentVerySatisfied)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        moodList.forEach { (moodName, color, icon) ->
            val isSelected = currentMood.equals(moodName, ignoreCase = true)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) TechPrimary else Color.Transparent,
                            shape = CircleShape
                        )
                        .background(color.copy(alpha = 0.3f))
                        .clickable { onMoodSelected(moodName, color) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = moodName,
                        tint = if (isSelected) color.copy(alpha = 1f) else color.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = moodName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = if (isSelected) TechTextPrimary else TechTextSecondary
                )
            }
        }
    }
}