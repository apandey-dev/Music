package com.amoled.music.data.backup

import android.content.Context
import android.os.Environment
import android.util.Log
import com.amoled.music.data.db.AppDatabase
import com.amoled.music.data.db.PlaylistEntity
import com.amoled.music.data.db.PlaylistSongEntity
import com.amoled.music.data.model.PlaylistWithSongs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistBackupManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val TAG = "PlaylistBackupManager"

    private fun getBackupDirectories(): List<File> {
        val dirs = mutableListOf<File>()

        // 1. Documents/AmoledMusic (Persists across uninstalls on Android 10+)
        val docDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "AmoledMusic")
        dirs.add(docDir)

        // 2. Music/AmoledPlaylists (Persists across uninstalls)
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "AmoledPlaylists")
        dirs.add(musicDir)

        // 3. App-specific external files (fallback)
        context.getExternalFilesDir(null)?.let { dirs.add(it) }

        return dirs
    }

    suspend fun saveBackup() = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val playlistEntities = db.playlistDao().getAllPlaylistsSync()

            val fullData = mutableListOf<PlaylistWithSongs>()
            for (p in playlistEntities) {
                val songEntities = db.playlistDao().getPlaylistSongs(p.id)
                val songs = songEntities.map { it.toSong() }
                fullData.add(PlaylistWithSongs(playlist = p.toPlaylist(songs.size), songs = songs))
            }

            val jsonString = gson.toJson(fullData)

            for (dir in getBackupDirectories()) {
                try {
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val backupFile = File(dir, "playlists_backup.json")
                    backupFile.writeText(jsonString)
                    Log.d(TAG, "Saved backup to: ${backupFile.absolutePath}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed writing backup to ${dir.absolutePath}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveBackup failed: ${e.message}", e)
        }
    }

    suspend fun autoRestoreIfEmpty() = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val existing = db.playlistDao().getAllPlaylistsSync()
            if (existing.isNotEmpty()) {
                Log.d(TAG, "Database already has playlists, skipping auto-restore.")
                return@withContext
            }

            var jsonContent: String? = null
            for (dir in getBackupDirectories()) {
                val file = File(dir, "playlists_backup.json")
                if (file.exists() && file.canRead()) {
                    jsonContent = file.readText()
                    Log.d(TAG, "Found backup file at: ${file.absolutePath}")
                    break
                }
            }

            if (jsonContent.isNullOrBlank()) {
                Log.d(TAG, "No backup file found to restore.")
                return@withContext
            }

            val type = object : TypeToken<List<PlaylistWithSongs>>() {}.type
            val restored: List<PlaylistWithSongs> = gson.fromJson(jsonContent, type) ?: emptyList()

            for (item in restored) {
                val p = item.playlist
                val playlistId = db.playlistDao().insertPlaylist(
                    PlaylistEntity(
                        name = p.name,
                        bannerUriString = p.bannerUriString,
                        createdAt = p.createdAt,
                        updatedAt = System.currentTimeMillis()
                    )
                )

                val songEntities = item.songs.mapIndexed { index, song ->
                    PlaylistSongEntity.fromSong(playlistId, song, index)
                }
                db.playlistDao().insertPlaylistSongs(songEntities)
            }
            Log.d(TAG, "Successfully restored ${restored.size} playlists from backup!")
        } catch (e: Exception) {
            Log.e(TAG, "autoRestoreIfEmpty failed: ${e.message}", e)
        }
    }
}
