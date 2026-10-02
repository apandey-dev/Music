package com.amoled.music.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val bannerUriString: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query("""
        SELECT p.id, p.name, p.bannerUriString, p.createdAt, p.updatedAt, 
               COUNT(s.entryId) as songCount
        FROM playlists p
        LEFT JOIN playlist_songs s ON p.id = s.playlistId
        GROUP BY p.id
        ORDER BY p.updatedAt DESC
    """)
    fun getAllPlaylistsFlow(): Flow<List<PlaylistWithCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getPlaylistById(id: Long): PlaylistEntity?

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistSongsFlow(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    suspend fun getPlaylistSongs(playlistId: Long): List<PlaylistSongEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSongs(songs: List<PlaylistSongEntity>)

    @Query("SELECT * FROM playlists ORDER BY id ASC")
    suspend fun getAllPlaylistsSync(): List<PlaylistEntity>
}
