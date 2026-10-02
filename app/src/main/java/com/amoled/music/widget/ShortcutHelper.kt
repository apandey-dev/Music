package com.amoled.music.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.amoled.music.MainActivity
import com.amoled.music.R
import com.amoled.music.data.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ShortcutHelper {

    suspend fun pinPlaylistToHome(context: Context, playlist: Playlist): Boolean = withContext(Dispatchers.IO) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Pinning shortcuts not supported on this launcher", Toast.LENGTH_SHORT).show()
            }
            return@withContext false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("amoledmusic://playlist?id=${playlist.id}&autoPlay=true")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val iconCompat = loadPlaylistIcon(context, playlist.bannerUriString, playlist.name)

        val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "playlist_${playlist.id}")
            .setShortLabel(playlist.name)
            .setLongLabel(playlist.name)
            .setIcon(iconCompat)
            .setIntent(intent)
            .build()

        val success = ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)

        withContext(Dispatchers.Main) {
            if (success) {
                Toast.makeText(context, "Added '${playlist.name}' to Home Screen!", Toast.LENGTH_SHORT).show()
            }
        }
        return@withContext success
    }

    private fun loadPlaylistIcon(context: Context, bannerUriString: String?, name: String): IconCompat {
        if (!bannerUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(bannerUriString)
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap != null) {
                    val size = 192
                    val roundedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(roundedBitmap)
                    val paint = Paint().apply { isAntiAlias = true }

                    val srcRect = android.graphics.Rect(0, 0, originalBitmap.width, originalBitmap.height)
                    val destRect = RectF(0f, 0f, size.toFloat(), size.toFloat())
                    canvas.drawRoundRect(destRect, 28f, 28f, paint)
                    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                    canvas.drawBitmap(originalBitmap, srcRect, destRect, paint)

                    return IconCompat.createWithBitmap(roundedBitmap)
                }
            } catch (e: Exception) {
                // Fallback to stylized bitmap
            }
        }

        // Generate clean AMOLED stylized icon with first letter
        val size = 192
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(0f, 0f, size.toFloat(), size.toFloat()), 32f, 32f, bgPaint)

        val borderPaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.STROKE
            strokeWidth = 6f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(3f, 3f, size - 3f, size - 3f), 32f, 32f, borderPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 72f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        val initial = name.firstOrNull()?.uppercase() ?: "M"
        val yOffset = (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(initial, size / 2f, (size / 2f) - yOffset, textPaint)

        return IconCompat.createWithBitmap(bitmap)
    }
}
