package com.example.mindlens.ui.components.article

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.helpers.formatDate
import com.example.mindlens.model.GetArticleComment
import com.example.mindlens.ui.TechTextPrimary

@Composable
fun CommentItem(comment: GetArticleComment) {
    // get the user's data from relation
    val userName = comment.profile?.fullName ?: "Unknown User"
    val avatarBase64 = comment.profile?.avatar

    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (avatarBase64 != null) {
                // convert image from Base64 to image
                val decodedBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                // display the formatted image
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                // use placeholder if user's avatar is null
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${userName}&background=random&color=fff",
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
        // spacer
        Spacer(modifier = Modifier.width(12.dp))

        // the comment content
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatDate(comment.createdAt ?: ""), color = Color.Gray, fontSize = 12.sp)
            }
            Text(comment.comment, fontSize = 14.sp, color = TechTextPrimary)
        }
    }
}