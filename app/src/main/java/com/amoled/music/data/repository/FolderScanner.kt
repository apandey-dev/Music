package com.amoled.music.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.amoled.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class FolderScanner(private val context: Context) {

    private val TAG = "FolderScanner"

    private val supportedExtensions = setOf(
        "mp3", "m4a", "wav", "flac", "aac", "ogg", "opus",
        "wma", "mka", "mid", "amr", "aiff", "3gp", "webm", "oga"
    )

    private fun isAudioFile(name: String?, mimeType: String?): Boolean {
        val lowerName = name?.lowercase(Locale.ROOT) ?: ""
        if (lowerName.contains(".nomedia") || lowerName.endsWith(".m3u") || lowerName.endsWith(".pls") || 
            lowerName.endsWith(".txt") || lowerName.endsWith(".jpg") || lowerName.endsWith(".png") ||
            lowerName.contains("all music") || lowerName.contains("all tracks") || lowerName.contains("all songs")) {
            return false
        }
        val ext = lowerName.substringAfterLast('.', "")
        if (ext in supportedExtensions) return true
        return mimeType?.startsWith("audio/") == true
    }

    suspend fun scanDocumentTreeUri(treeUri: Uri): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        try {
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
            scanDocumentFileRecursive(rootDoc, songs)
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning tree uri: ${e.message}", e)
        }
        songs
    }

    private fun scanDocumentFileRecursive(doc: DocumentFile, outList: MutableList<Song>) {
        if (doc.isDirectory) {
            val files = doc.listFiles()
            for (file in files) {
                scanDocumentFileRecursive(file, outList)
            }
        } else if (doc.isFile && doc.length() > 20480L && isAudioFile(doc.name, doc.type)) {
            val fileName = doc.name ?: "Unknown"
            if (!CallRecordingFilter.isCallRecording(fileName, doc.uri.path, fileName)) {
                val song = extractSongMetadata(doc.uri, fileName)
                if (song != null && !CallRecordingFilter.isCallRecording(song.title, song.dataPath, fileName)) {
                    outList.add(song)
                }
            }
        }
    }

    private fun extractSongMetadata(uri: Uri, fileName: String): Song? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L

            // Strict validation: exclude 0 duration or corrupt audio files (< 3 seconds)
            if (duration < 3000L) {
                return null
            }

            val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val title = if (!rawTitle.isNullOrBlank()) rawTitle.trim() else fileName.substringBeforeLast('.').trim()

            // Exclude dummy placeholder titles
            val lowerTitle = title.lowercase(Locale.ROOT)
            if (lowerTitle.contains("all music") || lowerTitle.contains("all songs") || lowerTitle.contains("all tracks")) {
                return null
            }

            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() }?.trim() ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() }?.trim() ?: "Unknown Album"

            Song(
                id = (uri.toString() + title + duration).hashCode().toLong(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                contentUriString = uri.toString(),
                albumArtUriString = null,
                dataPath = uri.path ?: ""
            )
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }
    }

    suspend fun scanDirectFileDirectory(dir: File): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()
        if (dir.exists() && dir.isDirectory) {
            dir.walkTopDown()
                .filter { it.isFile && isAudioFile(it.name, null) }
                .forEach { file ->
                    if (!CallRecordingFilter.isCallRecording(file.name, file.absolutePath, file.name)) {
                        val uri = Uri.fromFile(file)
                        val song = extractSongMetadata(uri, file.name)
                        if (song != null && !CallRecordingFilter.isCallRecording(song.title, song.dataPath, file.name)) {
                            songs.add(song)
                        }
                    }
                }
        }
        songs
    }
}
