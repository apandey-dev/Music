package com.amoled.music.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Playlist(
    @SerializedName("id") val id: Long = 0,
    @SerializedName("name") val name: String,
    @SerializedName("bannerUriString") val bannerUriString: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("songCount") val songCount: Int = 0
)

@Keep
data class PlaylistWithSongs(
    @SerializedName("playlist") val playlist: Playlist,
    @SerializedName("songs") val songs: List<Song>
)
