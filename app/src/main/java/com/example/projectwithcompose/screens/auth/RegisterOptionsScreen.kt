package com.example.projectwithcompose.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.projectwithcompose.R
import com.example.projectwithcompose.supabase.DatabaseConnection
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectwithcompose.Routes
import com.example.projectwithcompose.viewModel.AuthState
import com.example.projectwithcompose.viewModel.AuthViewModel
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth

val TextGray = Color(0xFF888888)
val BorderGray = Color(0xFFEEEEEE)
val InputBg = Color(0xFFFAFAFA) // Very light gray for inputs if needed, or White

fun onRegisterClick(email: String, password: String) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOptionsScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // State for inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // 2. REACTIVE NAVIGATION
    // As soon as the ViewModel says "Authenticated", this block runs and moves to Home
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Routes.MainApp) {
                popUpTo(Routes.RegisterOptions) { inclusive = true }
            }
        }
    }

    val loginAction = DatabaseConnection.supabase.composeAuth.rememberSignInWithGoogle(
        onResult = { result ->
            viewModel.handleGoogleResult(result)
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Register",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. Headline
            Text(
                text = "Let’s get started!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 28.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Subtitle
            Text(
                text = "Input your Email Address & Password",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextGray,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 3. Form Fields
            // Email
            CustomLabeledTextField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "example@email.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            CustomLabeledTextField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                placeholder = "Must contain atleast 6 character",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Google Button (Replaces "Already have account")
            SocialButton(
                text = "Continue with Google",
                iconRes = R.drawable.ic_google,
                onClick = { loginAction.startFlow() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Continue Button (Primary)
            Button(
                onClick = { onRegisterClick(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

// --- Helper Components ---

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

@Composable
fun SocialButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderGray),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Assuming you have a drawable resource
            // If not, replace painterResource with ImageVector logic
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified // Keep original logo colors
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}