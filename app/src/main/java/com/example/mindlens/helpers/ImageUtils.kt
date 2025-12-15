package com.example.mindlens.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale
import com.example.mindlens.R
import com.example.mindlens.dataClass.ScanHistoryItem
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

// utilities related with image
object ImageUtils {
    // convert image to base64
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Resize image to avoid massive strings
            val scaledBitmap = bitmap.scale(300, 300)

            val outputStream = ByteArrayOutputStream()
            // Compress to JPEG, Quality 70
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()

            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to decode Base64 back to ImageBitmap for display
    fun base64ToBitmap(base64Str: String): ImageBitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size).asImageBitmap()
    }

    // decode bitmap from URI
    fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val raw = if (Build.VERSION.SDK_INT < 28) {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.isMutableRequired = true
                }
            }
            // Ensure the bitmap is ARGB_8888 and mutable
            raw.copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: Exception) {
            Log.e("ERR_DECODE_BITMAP_FROM_URI", e.message.toString())
            null
        }
    }

    fun getMoodVectorIcon(mood: String): Pair<Int, Color> {
        return when (mood.lowercase()) {
            "great", "amazing", "bahagia" ->
                R.drawable.ic_verysatisfied to Color(0xFF64B5F6)

            "good", "senang" ->
                R.drawable.ic_satisfied to Color(0xFFAED581)

            "neutral", "okay", "biasa" ->
                R.drawable.ic_neutral to Color(0xFFFFF176)

            "bad", "buruk" ->
                R.drawable.ic_dissatisfied to Color(0xFFFFB74D)

            "awful", "terrible", "sedih" ->
                R.drawable.ic_verydissatisfied to Color(0xFFE57373)

            else ->
                R.drawable.ic_neutral to Color.Gray
        }
    }

    fun bitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorResId: Int,
        tint: Color
    ): BitmapDescriptor {

        val drawable = ContextCompat.getDrawable(context, vectorResId)
            ?: return BitmapDescriptorFactory.defaultMarker()

        drawable.setTint(tint.toArgb())

        val sizePx = 64
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}