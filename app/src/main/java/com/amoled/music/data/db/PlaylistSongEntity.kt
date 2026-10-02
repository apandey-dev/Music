package com.amoled.music.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.amoled.music.data.model.Song

@Entity(
    tableName = "playlist_songs",
    indices = [
        Index(value = ["playlistId"]),
        Index(value = ["playlistId", "orderIndex"])
    ]
)
data class PlaylistSongEntity(
    @PrimaryKey(autoGenerate = true)
    val entryId: Long = 0,
    val playlistId: Long,
    val songId: Long,
    val songTitle: String,
    val songArtist: String,
    val songAlbum: String,
    val songDuration: Long,
    val songContentUriString: String,
    val songAlbumArtUriString: String?,
    val songDataPath: String,
    val orderIndex: Int
) {
    fun toSong(): Song {
        return Song(
            id = songId,
            title = songTitle,
            artist = songArtist,
            album = songAlbum,
            duration = songDuration,
            contentUriString = songContentUriString,
            albumArtUriString = songAlbumArtUriString,
            dataPath = songDataPath
        )
    }

    companion object {
        fun fromSong(playlistId: Long, song: Song, orderIndex: Int): PlaylistSongEntity {
            return PlaylistSongEntity(
                playlistId = playlistId,
                songId = song.id,
                songTitle = song.title,
                songArtist = song.artist,
                songAlbum = song.album,
                songDuration = song.duration,
                songContentUriString = song.contentUriString,
                songAlbumArtUriString = song.albumArtUriString,
                songDataPath = song.dataPath,
                orderIndex = orderIndex
            )
        }
    }
}
