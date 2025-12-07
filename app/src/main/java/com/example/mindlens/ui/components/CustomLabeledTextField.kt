package com.example.mindlens.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindlens.screens.auth.BorderGray
import com.example.mindlens.screens.auth.TextGray

@Composable
fun CustomLabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Label outside the box
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextGray,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // The Input Box
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color(0xFFC4C4C4), // Light placeholder color
                    fontSize = 14.sp
                )
            },
            shape = RoundedCornerShape(12.dp), // Slightly rounded corners
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = Color.Black,
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = BorderGray,
            ),
            visualTransformation = if (isPassword && value.isNotEmpty()) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            singleLine = true
        )
    }
}