package com.amoled.music.data.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.amoled.music.data.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class AudioScanner(private val context: Context) {

    private val TAG = "AudioScanner"
    private val folderScanner = FolderScanner(context)

    private val supportedExtensions = setOf(
        "mp3", "m4a", "wav", "flac", "aac", "ogg", "opus",
        "wma", "mka", "mid", "amr", "aiff", "3gp", "webm", "oga"
    )

    suspend fun scanDeviceSongs(customFolderUris: List<Uri> = emptyList()): List<Song> = withContext(Dispatchers.IO) {
        val songMap = LinkedHashMap<String, Song>()

        if (customFolderUris.isNotEmpty()) {
            // 1. User has chosen specific folders: scan ONLY those folders!
            for (uri in customFolderUris) {
                try {
                    val folderSongs = folderScanner.scanDocumentTreeUri(uri)
                    for (song in folderSongs) {
                        songMap[song.contentUriString] = song
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error scanning custom folder uri $uri: ${e.message}")
                }
            }
        } else {
            // 2. Default: Scan standard MediaStore music + Standard Music / Download directories
            val mediaStoreSongs = scanMediaStore()
            for (song in mediaStoreSongs) {
                val key = if (song.dataPath.isNotBlank()) song.dataPath else song.title + song.artist
                songMap[key] = song
            }

            val defaultDirs = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                File(Environment.getExternalStorageDirectory(), "Music"),
                File(Environment.getExternalStorageDirectory(), "Download")
            )

            for (dir in defaultDirs) {
                try {
                    if (dir.exists() && dir.canRead()) {
                        val diskSongs = folderScanner.scanDirectFileDirectory(dir)
                        for (song in diskSongs) {
                            val key = if (song.dataPath.isNotBlank()) song.dataPath else song.title + song.artist
                            if (!songMap.containsKey(key)) {
                                songMap[key] = song
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error scanning default dir ${dir.absolutePath}: ${e.message}")
                }
            }
        }

        songMap.values.sortedBy { it.title.lowercase(Locale.ROOT) }
    }

    private fun scanMediaStore(): List<Song> {
        val songs = mutableListOf<Song>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE
        )

        // Avoid short clips, system ringtones, notifications, and alarms
        val selection = "${MediaStore.Audio.Media.DURATION} >= 3000 " +
                "AND (${MediaStore.Audio.Media.IS_RINGTONE} == 0 OR ${MediaStore.Audio.Media.IS_RINGTONE} IS NULL) " +
                "AND (${MediaStore.Audio.Media.IS_NOTIFICATION} == 0 OR ${MediaStore.Audio.Media.IS_NOTIFICATION} IS NULL) " +
                "AND (${MediaStore.Audio.Media.IS_ALARM} == 0 OR ${MediaStore.Audio.Media.IS_ALARM} IS NULL)"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val mimeTypeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    var title = cursor.getString(titleCol)
                    val artist = cursor.getString(artistCol) ?: "<unknown>"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val duration = cursor.getLong(durationCol)
                    val albumId = cursor.getLong(albumIdCol)
                    val dataPath = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                    val displayName = if (displayNameCol != -1) cursor.getString(displayNameCol) ?: "" else ""
                    val mimeType = if (mimeTypeCol != -1) cursor.getString(mimeTypeCol) ?: "" else ""

                    if (duration < 3000L) {
                        continue
                    }

                    if (title.isNullOrBlank()) {
                        title = displayName.substringBeforeLast('.').ifBlank { "Track $id" }
                    }

                    val lowerTitle = title.lowercase(Locale.ROOT)
                    if (lowerTitle.contains("all music") || lowerTitle.contains("all tracks") || lowerTitle.contains("all songs")) {
                        continue
                    }

                    if (CallRecordingFilter.isCallRecording(title, dataPath, displayName)) {
                        continue
                    }

                    val ext = (if (dataPath.isNotBlank()) dataPath else displayName)
                        .substringAfterLast('.', "")
                        .lowercase(Locale.ROOT)

                    if (mimeType.startsWith("audio/") || ext in supportedExtensions) {
                        val contentUri = ContentUris.withAppendedId(collection, id)
                        val albumArtUri = ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        )

                        val cleanArtist = if (artist == "<unknown>" || artist.isBlank()) "Unknown Artist" else artist

                        songs.add(
                            Song(
                                id = id,
                                title = title,
                                artist = cleanArtist,
                                album = album,
                                duration = duration,
                                contentUriString = contentUri.toString(),
                                albumArtUriString = albumArtUri.toString(),
                                dataPath = dataPath
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore: ${e.message}", e)
        }

        return songs
    }
}
