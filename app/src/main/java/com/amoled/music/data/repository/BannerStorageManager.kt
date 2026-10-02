package com.amoled.music.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object BannerStorageManager {

    suspend fun saveBannerLocally(context: Context, sourceUri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val bannersDir = File(context.filesDir, "banners")
            if (!bannersDir.exists()) {
                bannersDir.mkdirs()
            }

            val inputStream = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val fileName = "banner_${System.currentTimeMillis()}.jpg"
                val destFile = File(bannersDir, fileName)
                val fos = FileOutputStream(destFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()
                return@withContext Uri.fromFile(destFile).toString()
            }
        } catch (e: Exception) {
            // Fallback to original string if error
        }
        return@withContext sourceUri.toString()
    }
}
