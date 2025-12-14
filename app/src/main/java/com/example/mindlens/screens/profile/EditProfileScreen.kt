package com.example.mindlens.screens.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.input.CustomTextField
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.ui.components.element.SimpleTopBar
import com.example.mindlens.viewModels.ProfileViewModel
import kotlinx.coroutines.launch

// ----------------- EDIT PROFILE SCREEN -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Toast States
    var toastVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isToastError by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Scaffold(
        topBar = { SimpleTopBar("Edit Profile", onBack) }
    ) { padding ->
        // Show Loading Spinner while fetching data
        if (profileViewModel.isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TechPrimary)
            }
        } else {
            // Show Form once data is loaded
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(TechBackground)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier
                        .size(100.dp)
                        .clickable { launcher.launch("image/*") }
                ) {
                    // Logic: Show New Selection -> OR Show Saved Base64 -> OR Show Google URL -> OR Placeholder
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else if (!profileViewModel.currentAvatarBase64.value.isNullOrEmpty()) {
                        // Convert Base64 string to Bitmap for Display
                        val cleanBase64 = profileViewModel.currentAvatarBase64.value!!
                        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Saved Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val username  = profileViewModel.name.value
                        AsyncImage(
                            model = profileViewModel.googleAvatarUrl.value ?: "https://ui-avatars.com/api/?name=${username}&background=random&color=fff",
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Camera Icon Overlay
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(TechPrimary, CircleShape)
                            .padding(6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White)
                    }
                }

                // Input fields
                CustomTextField(
                    label = "Full Name",
                    value = profileViewModel.name.value,
                    onValueChange = { profileViewModel.name.value = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = "Email Address",
                    value = profileViewModel.email.value,
                    onValueChange = { /* Email is read-only */ },
                    isReadOnly = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                CustomTextField(
                    label = "Bio",
                    value = profileViewModel.bio.value,
                    onValueChange = { profileViewModel.bio.value = it },
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        profileViewModel.saveChanges(
                            context = context,
                            newImageUri = selectedImageUri,
                            onSuccess = {
                                profileViewModel.showSuccessMessage.value = true

                                onBack()
                            },
                            onError = { msg ->
                                toastMessage = "Error: $msg"
                                toastVisible = true
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        CustomToast(
            visible = toastVisible,
            message = toastMessage,
            isError = true,
            onDismiss = { toastVisible = false }
        )
    }
}