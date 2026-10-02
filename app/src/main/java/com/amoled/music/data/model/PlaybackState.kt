package com.amoled.music.data.model

import androidx.annotation.Keep

@Keep
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Song> = emptyList(),
    val currentTrackIndex: Int = -1,
    val currentPlaylistName: String = "",
    val currentPlaylistId: Long? = null
) {
    val progress: Float
        get() = if (totalDurationMs > 0) (currentPositionMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
}

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}
