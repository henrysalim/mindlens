package com.example.mindlens.ui.components.article

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mindlens.helpers.ImageUtils
import com.example.mindlens.helpers.formatDate
import com.example.mindlens.model.GetArticleComment
import com.example.mindlens.ui.TechTextPrimary

@Composable
fun CommentTree(
    comment: GetArticleComment,
    onReplyClick: (GetArticleComment) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        // Render Parent Comment
        CommentItem(comment = comment, onReplyClick = onReplyClick)

        // Render Replies (Anak-anaknya) dengan Indentation (Padding Kiri)
        if (comment.replies.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 48.dp, top = 8.dp)) {
                comment.replies.forEach { reply ->
                    CommentItem(
                        comment = reply,
                        onReplyClick = onReplyClick, // Reply ke reply juga bisa
                        isReplyItem = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: GetArticleComment,
    onReplyClick: (GetArticleComment) -> Unit,
    isReplyItem: Boolean = false
) {
    val userName = comment.profile?.fullName ?: "Unknown User"
    val avatarBase64 = comment.profile?.avatar

    // Ukuran avatar lebih kecil sedikit jika itu adalah komentar balasan
    val avatarSize = if (isReplyItem) 32.dp else 40.dp
    val fontSize = if (isReplyItem) 13.sp else 14.sp

    Row(verticalAlignment = Alignment.Top) {
        // --- AVATAR SECTION ---
        Box(
            modifier = Modifier.size(avatarSize).clip(CircleShape).background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            if (avatarBase64 != null) {
                val bitmap = ImageUtils.base64ToBitmap(avatarBase64)
                Image(
                    bitmap = bitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                AsyncImage(
                    model = "https://ui-avatars.com/api/?name=${userName}&background=random&color=fff",
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- CONTENT SECTION ---
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userName, fontWeight = FontWeight.Bold, fontSize = fontSize)
                Spacer(modifier = Modifier.width(8.dp))
                Text(formatDate(comment.createdAt ?: ""), color = Color.Gray, fontSize = 12.sp)
            }

            Text(comment.comment, fontSize = fontSize, color = TechTextPrimary)

            // Tombol Reply
            Text(
                text = "Reply",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable { onReplyClick(comment) } // Trigger event reply
            )
        }
    }
}