package com.example.mindlens.ui.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.ui.TechTextPrimary

@Composable
fun StatItem(
    mood: String,
    count: Int
) {
    val (iconRes, color) = ImageUtils.getMoodVectorIcon(mood)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = mood,
                tint = color,
                modifier = Modifier.size(18.dp).graphicsLayer { alpha = 0.9f }
            )

            Text(
                text = mood,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = TechTextPrimary
            )
        }

        Text(
            text = "$count",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TechTextPrimary
        )
    }
}