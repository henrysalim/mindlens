package com.example.mindlens.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mindlens.R
import com.example.mindlens.supabase.DatabaseConnection
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindlens.navigations.Routes
import com.example.mindlens.ui.components.input.CustomLabeledTextField
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.ui.components.element.SocialButton
import com.example.mindlens.viewModels.AuthState
import com.example.mindlens.viewModels.AuthViewModel
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.launch

val TextGray = Color(0xFF888888)
val BorderGray = Color(0xFFEEEEEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterOptionsScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    // Toast States
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isToastError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Input States
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

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
            // top bar
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

            // Full Name
            CustomLabeledTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = "John Doe",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            CustomLabeledTextField(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Must same with your password",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. Google Button
            SocialButton(
                text = "Continue with Google",
                iconRes = R.drawable.ic_google,
                onClick = { loginAction.startFlow() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Submit button
            Button(
                onClick = {
                    isLoading = true
                    // 1. Basic Validation
                    if (password != confirmPassword) {
                        toastMessage = "Passwords do not match!"
                        isToastError = true
                        showToast = true
                    } else if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
                        toastMessage = "Please fill all fields"
                        isToastError = true
                        showToast = true
                    } else if (password.length < 8 || confirmPassword.length < 8) {
                        toastMessage = "Password length must be at least 8 characters"
                        isToastError = true
                        showToast = true
                    } else {
                        // 2. Call ViewModel
                        viewModel.signUp(
                            email = email,
                            pass = password,
                            fullName = fullName,
                            onSuccess = {
                                // Redirect to Native Login Screen if success
                                toastMessage = "Registration Successful! Redirecting..."
                                isToastError = false
                                showToast = true

                                scope.launch {
                                    kotlinx.coroutines.delay(2000) // Wait 2s
                                    navController.navigate(Routes.NativeLogin) {
                                        // Optional: Prevent going back to register
                                        popUpTo(Routes.RegisterOptions) { inclusive = true }
                                    }
                                }
                            },
                            onError = { errorMessage ->
                                // If error: Show Custom Red Toast
                                toastMessage = errorMessage
                                isToastError = true
                                showToast = true
                            }
                        )
                    }
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                enabled = if(isLoading) false else true,
            ) {
                if (isLoading) {
                    // displya circular indicator if still loading
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    // display text if not in loading state
                    Text("Log In")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // move to login page button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                TextButton(
                    onClick = {
                        // Navigate to the Login Screen
                        navController.navigate(Routes.NativeLogin) {
                            popUpTo(Routes.RegisterOptions) { inclusive = true }
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Login",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }

            // toast to display messages
            CustomToast(
                visible = showToast,
                message = toastMessage,
                isError = isToastError,
                onDismiss = { showToast = false }
            )
        }
    }
}