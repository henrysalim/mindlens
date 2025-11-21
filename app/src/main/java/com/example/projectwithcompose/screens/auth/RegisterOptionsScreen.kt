import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.projectwithcompose.R

// Define specific colors from the design
val FacebookBlue = Color(0xFF425893)
val TextGray = Color(0xFF888888)
val BorderGray = Color(0xFFEEEEEE)

@Composable
fun RegisterOptionsScreen(onEmailClick: () -> Unit) {
    Scaffold(
        topBar = {
            // Custom Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Title
                Text(
                    text = "Register",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = Color.White // Set background to white
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp), // Side margins
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Heading
            Text(
                text = "Let’s get started!",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subheading
            Text(
                text = "Input your Email Address & Password",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextGray,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(32.dp))


            SocialLoginButton(
                text = "Continue with Google",
                // Google usually requires a custom Drawable resource because it's multi-colored
                // passing a placeholder here
                icon = ImageVector.vectorResource(R.drawable.ic_google), // REPLACE with R.drawable.ic_google_logo
                backgroundColor = Color.White,
                textColor = Color.Black,
                borderColor = BorderGray,
                isGoogle = true // Helper to handle the icon tint logic
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Footer Link
            TextButton(onClick = onEmailClick) { // <--- Wrap in TextButton
                Text(
                    text = "Or Use Email",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

// --- Reusable Component ---
@Composable
fun SocialLoginButton(
    text: String,
    icon: ImageVector, // Change to Painter if using R.drawable.xxx
    backgroundColor: Color,
    textColor: Color,
    borderColor: Color? = null,
    isGoogle: Boolean = false
) {
    Button(
        onClick = { /* TODO */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp), // Standard height for touch targets
        shape = RoundedCornerShape(8.dp), // Rounded corners as per design
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        border = if (borderColor != null) BorderStroke(1.dp, borderColor) else null,
        elevation = ButtonDefaults.buttonElevation(0.dp) // Flat design
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                // Google logo is usually original colors, others are tinted to match text
                tint = if (isGoogle) Color.Unspecified else textColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Preview
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()

    RegisterOptionsScreen(
        onEmailClick = {
            navController.navigate("register_email")
        }
    )
}