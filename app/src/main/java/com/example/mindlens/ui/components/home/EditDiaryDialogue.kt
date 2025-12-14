package com.example.mindlens.ui.components.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.model.DiaryEntry
import com.example.mindlens.ui.TechPrimary
import com.example.mindlens.ui.TechSurface
import com.example.mindlens.ui.TechTextPrimary
import com.example.mindlens.ui.TechTextSecondary

@Composable
fun EditDiaryDialogue(
    entry: DiaryEntry,
    onDismiss: () -> Unit,
    onConfirm: (DiaryEntry) -> Unit
) {
    var editTitle by remember { mutableStateOf(entry.title) }
    var editContent by remember { mutableStateOf(entry.content) }
    var editMood by remember { mutableStateOf(entry.mood) }
    var editColor by remember { mutableStateOf(entry.color) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TechSurface,
        title = { Text("Edit Diary", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
        text = {
            Column {
                Text("Title", fontSize = 14.sp, color = TechTextSecondary)
                OutlinedTextField(
                    value = editTitle,
                    onValueChange = { editTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechPrimary,
                        unfocusedBorderColor = TechTextSecondary,
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Change Mood", fontSize = 14.sp, color = TechTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                EditMoodSelector(
                    currentMood = editMood,
                    onMoodSelected = { mood, color ->
                        editMood = mood
                        editColor = color.toArgb().toLong()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Content", fontSize = 14.sp, color = TechTextSecondary)
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TechPrimary,
                        unfocusedBorderColor = TechTextSecondary,
                        focusedTextColor = TechTextPrimary,
                        unfocusedTextColor = TechTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedData = entry.copy(
                        title = editTitle,
                        content = editContent,
                        mood = editMood,
                        color = editColor
                    )
                    onConfirm(updatedData)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary, contentColor = Color.White)
            ) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}