package com.example.mindlens.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.mindlens.Routes
import com.example.mindlens.supabase.DatabaseConnection
import com.example.mindlens.ui.components.CustomLabeledTextField
import com.example.mindlens.ui.components.CustomToast
import com.example.mindlens.viewModel.AuthState
import com.example.mindlens.viewModel.AuthViewModel
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.launch

fun onLoginClick(username: String, password: String) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeLoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    // State for inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // --- Toast States ---
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isToastError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // As soon as the ViewModel says "Authenticated", this block runs and moves to Home
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            navController.navigate(Routes.MainApp) {
                popUpTo(Routes.RegisterOptions) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )
                    )
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

            // 5. Continue Button (Primary)
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        toastMessage = "Please fill all fields"
                        isToastError = true
                        showToast = true
                    } else {
                        viewModel.signIn(
                            email = email,
                            pass = password,
                            onSuccess = {
                                // SUCCESS: Redirect to Native Login Screen
                                toastMessage = "Login Successful! Redirecting..."
                                isToastError = false
                                showToast = true

                                scope.launch {
                                    kotlinx.coroutines.delay(2000) // Wait 2s
                                    navController.navigate(Routes.MainApp) {
                                        // Optional: Prevent going back to register
                                        popUpTo(Routes.NativeLogin) { inclusive = true }
                                    }
                                }
                            },
                            onError = { error ->
                                // FAILURE: Show Red Toast
                                toastMessage = error
                                isToastError = true
                                showToast = true
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = if(isLoading) false else true,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Log In")
                }
            }


            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = {
                        // Navigate to the Login Screen
                        navController.navigate(Routes.RegisterOptions) {
                            popUpTo(Routes.NativeLogin) { inclusive = true }
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Register here",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
                // This sits on top of the screen (z-index)
            CustomToast(
                visible = showToast,
                message = toastMessage,
                isError = isToastError,
                onDismiss = { showToast = false }
            )
        }
    }
}