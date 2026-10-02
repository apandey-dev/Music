package com.amoled.music.data.repository

import android.content.Context
import com.amoled.music.data.backup.PlaylistBackupManager
import com.amoled.music.data.db.AppDatabase
import com.amoled.music.data.db.PlaylistEntity
import com.amoled.music.data.db.PlaylistSongEntity
import com.amoled.music.data.db.PlaylistWithCount
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.PlaylistWithSongs
import com.amoled.music.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val playlistDao = db.playlistDao()
    private val audioScanner = AudioScanner(context)
    private val backupManager = PlaylistBackupManager(context)
    val folderManager = FolderManager(context)

    val allPlaylistsFlow: Flow<List<Playlist>> = playlistDao.getAllPlaylistsFlow().map { list ->
        list.map { item ->
            Playlist(
                id = item.id,
                name = item.name,
                bannerUriString = item.bannerUriString,
                createdAt = item.createdAt,
                songCount = item.songCount
            )
        }
    }

    suspend fun getDeviceSongs(): List<Song> {
        val customFolders = folderManager.getCustomFolderUris()
        return audioScanner.scanDeviceSongs(customFolders)
    }

    suspend fun addMusicFolder(uri: android.net.Uri): List<Song> {
        folderManager.addCustomFolder(uri)
        return getDeviceSongs()
    }

    fun getPlaylistSongsFlow(playlistId: Long): Flow<List<Song>> {
        return playlistDao.getPlaylistSongsFlow(playlistId).map { entities ->
            entities.map { it.toSong() }
        }
    }

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null
        val songs = playlistDao.getPlaylistSongs(playlistId)
        return entity.toPlaylist(songCount = songs.size)
    }

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        return playlistDao.getPlaylistSongs(playlistId).map { it.toSong() }
    }

    suspend fun getPlaylistWithSongs(playlistId: Long): PlaylistWithSongs? {
        val entity = playlistDao.getPlaylistById(playlistId) ?: return null
        val songs = playlistDao.getPlaylistSongs(playlistId).map { it.toSong() }
        return PlaylistWithSongs(
            playlist = entity.toPlaylist(songCount = songs.size),
            songs = songs
        )
    }

    suspend fun createPlaylist(name: String, bannerUri: String?, songs: List<Song>): Long {
        val id = playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name.trim().ifEmpty { "My Playlist" },
                bannerUriString = bannerUri,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        val songEntities = songs.mapIndexed { index, song ->
            PlaylistSongEntity.fromSong(id, song, index)
        }
        playlistDao.insertPlaylistSongs(songEntities)
        backupManager.saveBackup()
        return id
    }

    suspend fun updatePlaylist(playlistId: Long, name: String, bannerUri: String?, songs: List<Song>) {
        val existing = playlistDao.getPlaylistById(playlistId) ?: return
        playlistDao.updatePlaylist(
            existing.copy(
                name = name.trim().ifEmpty { existing.name },
                bannerUriString = bannerUri ?: existing.bannerUriString,
                updatedAt = System.currentTimeMillis()
            )
        )
        playlistDao.clearPlaylistSongs(playlistId)
        val songEntities = songs.mapIndexed { index, song ->
            PlaylistSongEntity.fromSong(playlistId, song, index)
        }
        playlistDao.insertPlaylistSongs(songEntities)
        backupManager.saveBackup()
    }

    suspend fun addSongToPlaylist(playlistId: Long, song: Song) {
        val existingSongs = playlistDao.getPlaylistSongs(playlistId)
        if (existingSongs.none { it.songId == song.id }) {
            val newEntity = PlaylistSongEntity.fromSong(playlistId, song, existingSongs.size)
            playlistDao.insertPlaylistSongs(listOf(newEntity))
            backupManager.saveBackup()
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
        playlistDao.clearPlaylistSongs(playlistId)
        backupManager.saveBackup()
    }

    suspend fun autoRestorePlaylists() {
        backupManager.autoRestoreIfEmpty()
    }
}
