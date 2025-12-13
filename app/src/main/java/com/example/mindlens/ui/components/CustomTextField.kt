package com.example.mindlens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isReadOnly: Boolean = false,
    maxLines: Int = 1
) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = TechTextSecondary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = maxLines,
            readOnly = isReadOnly,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TechPrimary,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = if (isReadOnly) Color(0xFFF0F0F0) else Color.White,
                unfocusedContainerColor = if (isReadOnly) Color(0xFFF0F0F0) else Color.White
            )
        )
    }
}