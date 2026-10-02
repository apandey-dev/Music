package com.amoled.music.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.amoled.music.data.model.Playlist

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bannerUriString: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toPlaylist(songCount: Int = 0): Playlist {
        return Playlist(
            id = id,
            name = name,
            bannerUriString = bannerUriString,
            createdAt = createdAt,
            songCount = songCount
        )
    }
}
