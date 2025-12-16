package com.example.mindlens.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.ui.*
import com.example.mindlens.ui.components.input.CustomTextField
import com.example.mindlens.ui.components.element.CustomToast
import com.example.mindlens.viewModels.ProfileViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import android.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    profileViewModel: ProfileViewModel
) {

    // Toast States
    var toastVisible by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Image Picker Launcher
    val context = LocalContext.current
    var croppedAvatar by remember { mutableStateOf<Bitmap?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val original = uriToBitmap(context, it)
            val cropped = cropCenterSquare(original)
            val compressed = compressBitmap(cropped)
            croppedAvatar = compressed
        }
    }


    Scaffold(
        topBar = {
            // the top bar
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = TechTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TechTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechBackground)
            )
        },
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
                    when {
                        croppedAvatar != null -> {
                            Image(
                                bitmap = croppedAvatar!!.asImageBitmap(),
                                contentDescription = "Cropped Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        !profileViewModel.currentAvatarBase64.value.isNullOrEmpty() -> {
                            Image(
                                bitmap = ImageUtils
                                    .base64ToBitmap(profileViewModel.currentAvatarBase64.value!!)
                                    .asImageBitmap(),
                                contentDescription = "Saved Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            val username = profileViewModel.name.value
                            AsyncImage(
                                model = profileViewModel.googleAvatarUrl.value
                                    ?: "https://ui-avatars.com/api/?name=${username}&background=random&color=fff",
                                contentDescription = "Avatar",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

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
                        val avatarBase64 = croppedAvatar?.let {
                            ImageUtils.bitmapToBase64(it)
                        }

                        profileViewModel.saveChanges(
                            context = context,
                            newAvatarBase64 = avatarBase64,
                            onSuccess = {
                                onBack()
                            },
                            onError = { msg ->
                                toastMessage = msg
                                toastVisible = true
                            }
                        )
                    }
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        // toast to display messages
        CustomToast(
            visible = toastVisible,
            message = toastMessage,
            isError = true,
            onDismiss = { toastVisible = false }
        )
    }
}

fun cropCenterSquare(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    val newSize = minOf(width, height)

    val xOffset = (width - newSize) / 2
    val yOffset = (height - newSize) / 2

    return Bitmap.createBitmap(
        bitmap,
        xOffset,
        yOffset,
        newSize,
        newSize
    )
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }
}

fun compressBitmap(bitmap: Bitmap, quality: Int = 85): Bitmap {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
    val byteArray = outputStream.toByteArray()
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
