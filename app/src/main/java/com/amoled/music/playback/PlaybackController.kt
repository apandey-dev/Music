package com.amoled.music.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.RepeatMode
import com.amoled.music.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlaybackController(private val context: Context) {

    private val TAG = "PlaybackController"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var currentPlaylistSongs: List<Song> = emptyList()
    private var progressTickerJob: Job? = null

    init {
        initMediaController()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                restoreQueueFromPlayer()
                setupPlayerListener()
                updateState()
                startProgressTicker()
            } catch (e: Exception) {
                Log.e(TAG, "Failed initializing MediaController: ${e.message}", e)
            }
        }, MoreExecutors.directExecutor())
    }

    private fun restoreQueueFromPlayer() {
        val player = mediaController ?: return
        val count = player.mediaItemCount
        if (count > 0 && currentPlaylistSongs.isEmpty()) {
            val restoredList = mutableListOf<Song>()
            var playlistName = "Playing"
            var playlistId = 0L

            for (i in 0 until count) {
                val item = player.getMediaItemAt(i)
                val metadata = item.mediaMetadata
                val extras = metadata.extras
                val title = metadata.title?.toString() ?: "Unknown"
                val artist = metadata.artist?.toString() ?: "Unknown Artist"
                val album = metadata.albumTitle?.toString() ?: "Unknown Album"
                val artUri = metadata.artworkUri?.toString()
                val uriStr = item.requestMetadata.mediaUri?.toString()
                    ?: item.localConfiguration?.uri?.toString()
                    ?: ""
                val id = item.mediaId.toLongOrNull() ?: extras?.getLong("song_id") ?: (i.toLong() + 1)
                val duration = extras?.getLong("duration") ?: 0L
                val dataPath = extras?.getString("data_path") ?: ""

                if (extras?.containsKey("playlist_name") == true) {
                    playlistName = extras.getString("playlist_name") ?: playlistName
                }
                if (extras?.containsKey("playlist_id") == true) {
                    playlistId = extras.getLong("playlist_id")
                }

                restoredList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        contentUriString = uriStr,
                        albumArtUriString = artUri,
                        dataPath = dataPath
                    )
                )
            }
            currentPlaylistSongs = restoredList
            val currentIndex = player.currentMediaItemIndex
            val currentSong = if (currentIndex in restoredList.indices) restoredList[currentIndex] else null

            _playbackState.update {
                it.copy(
                    queue = restoredList,
                    currentPlaylistName = playlistName,
                    currentPlaylistId = playlistId,
                    currentTrackIndex = currentIndex,
                    currentSong = currentSong,
                    isPlaying = player.isPlaying,
                    currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                    totalDurationMs = if (player.duration > 0) player.duration else (currentSong?.duration ?: 0L)
                )
            }
        }
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                updateState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (currentPlaylistSongs.isEmpty()) {
                    restoreQueueFromPlayer()
                }
                val index = mediaController?.currentMediaItemIndex ?: -1
                val song = if (index in currentPlaylistSongs.indices) currentPlaylistSongs[index] else extractSongFromMediaItem(mediaItem)
                _playbackState.update {
                    it.copy(
                        currentSong = song ?: it.currentSong,
                        currentTrackIndex = index,
                        totalDurationMs = mediaController?.duration?.coerceAtLeast(0L) ?: (song?.duration ?: 0L)
                    )
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                val mode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
                _playbackState.update { it.copy(repeatMode = mode) }
            }
        })
    }

    private fun extractSongFromMediaItem(mediaItem: MediaItem?): Song? {
        if (mediaItem == null) return null
        val metadata = mediaItem.mediaMetadata
        val extras = metadata.extras
        return Song(
            id = mediaItem.mediaId.toLongOrNull() ?: extras?.getLong("song_id") ?: 0L,
            title = metadata.title?.toString() ?: "Unknown",
            artist = metadata.artist?.toString() ?: "Unknown Artist",
            album = metadata.albumTitle?.toString() ?: "Unknown Album",
            duration = extras?.getLong("duration") ?: 0L,
            contentUriString = mediaItem.requestMetadata.mediaUri?.toString() ?: "",
            albumArtUriString = metadata.artworkUri?.toString(),
            dataPath = extras?.getString("data_path") ?: ""
        )
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = scope.launch {
            while (isActive) {
                val player = mediaController
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.coerceAtLeast(0L)
                    val dur = player.duration.coerceAtLeast(0L)
                    _playbackState.update {
                        it.copy(
                            currentPositionMs = pos,
                            totalDurationMs = if (dur > 0) dur else it.currentSong?.duration ?: 0L,
                            isPlaying = true
                        )
                    }
                }
                delay(300)
            }
        }
    }

    private fun updateState() {
        val player = mediaController ?: return
        if (currentPlaylistSongs.isEmpty() && player.mediaItemCount > 0) {
            restoreQueueFromPlayer()
        }

        val isPlaying = player.isPlaying
        val index = player.currentMediaItemIndex
        val song = if (index in currentPlaylistSongs.indices) currentPlaylistSongs[index] else extractSongFromMediaItem(player.currentMediaItem)
        val duration = player.duration.coerceAtLeast(0L)

        _playbackState.update {
            it.copy(
                isPlaying = isPlaying,
                currentTrackIndex = index,
                currentSong = song ?: it.currentSong,
                currentPositionMs = player.currentPosition.coerceAtLeast(0L),
                totalDurationMs = if (duration > 0) duration else (song?.duration ?: it.totalDurationMs),
                queue = currentPlaylistSongs
            )
        }
    }

    fun playPlaylist(
        playlist: Playlist,
        songs: List<Song>,
        startIndex: Int = 0,
        startPlaying: Boolean = true
    ) {
        if (songs.isEmpty()) return
        currentPlaylistSongs = ArrayList(songs)

        val player = mediaController ?: return
        player.clearMediaItems()

        val mediaItems = songs.map { song ->
            val extras = Bundle().apply {
                putLong("song_id", song.id)
                putString("data_path", song.dataPath)
                putLong("duration", song.duration)
                putString("playlist_name", playlist.name)
                putLong("playlist_id", playlist.id)
            }

            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.albumArtUri ?: playlist.bannerUriString?.let { Uri.parse(it) })
                .setExtras(extras)
                .build()

            MediaItem.Builder()
                .setUri(song.contentUri)
                .setMediaId(song.id.toString())
                .setMediaMetadata(metadata)
                .build()
        }

        player.setMediaItems(mediaItems, startIndex.coerceIn(0, mediaItems.size - 1), 0L)
        player.prepare()

        if (startPlaying) {
            player.play()
        }

        _playbackState.update {
            it.copy(
                queue = songs,
                currentPlaylistName = playlist.name,
                currentPlaylistId = playlist.id,
                currentTrackIndex = startIndex,
                currentSong = songs.getOrNull(startIndex),
                isPlaying = startPlaying
            )
        }
    }

    fun playSongFromCurrentQueue(index: Int) {
        val player = mediaController ?: return
        if (index in currentPlaylistSongs.indices) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    fun togglePlay() {
        val player = mediaController ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE || player.mediaItemCount == 0) {
                if (currentPlaylistSongs.isNotEmpty()) {
                    player.prepare()
                }
            }
            player.play()
        }
        _playbackState.update { it.copy(isPlaying = player.isPlaying) }
    }

    fun next() {
        val player = mediaController ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else if (currentPlaylistSongs.isNotEmpty()) {
            player.seekTo(0, 0L)
        }
    }

    fun previous() {
        val player = mediaController ?: return
        if (player.currentPosition > 3000) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaController ?: return
        player.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun toggleShuffle() {
        val player = mediaController ?: return
        val nextMode = !player.shuffleModeEnabled
        player.shuffleModeEnabled = nextMode
        _playbackState.update { it.copy(isShuffleEnabled = nextMode) }
    }

    fun toggleRepeat() {
        val player = mediaController ?: return
        val nextMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = nextMode
        val enumMode = when (nextMode) {
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            else -> RepeatMode.OFF
        }
        _playbackState.update { it.copy(repeatMode = enumMode) }
    }

    fun addSongToQueueNext(song: Song) {
        val player = mediaController ?: return
        val currentIndex = player.currentMediaItemIndex
        val targetIndex = if (currentPlaylistSongs.isEmpty()) 0 else (currentIndex + 1).coerceAtMost(currentPlaylistSongs.size)
        val mutable = currentPlaylistSongs.toMutableList()
        mutable.add(targetIndex, song)
        currentPlaylistSongs = mutable

        val extras = Bundle().apply {
            putLong("song_id", song.id)
            putString("data_path", song.dataPath)
            putLong("duration", song.duration)
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .setExtras(extras)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(song.contentUri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(metadata)
            .build()

        player.addMediaItem(targetIndex, mediaItem)
        _playbackState.update { it.copy(queue = mutable) }
    }

    fun playNext(index: Int) {
        val player = mediaController ?: return
        val currentIndex = player.currentMediaItemIndex
        if (index in currentPlaylistSongs.indices && index != currentIndex) {
            val targetIndex = (currentIndex + 1).coerceAtMost(currentPlaylistSongs.size - 1)
            val mutable = currentPlaylistSongs.toMutableList()
            val song = mutable.removeAt(index)
            mutable.add(targetIndex, song)
            currentPlaylistSongs = mutable

            player.moveMediaItem(index, targetIndex)
            _playbackState.update { it.copy(queue = mutable) }
        }
    }

    fun removeQueueItem(index: Int) {
        val player = mediaController ?: return
        if (index in currentPlaylistSongs.indices) {
            val mutable = currentPlaylistSongs.toMutableList()
            mutable.removeAt(index)
            currentPlaylistSongs = mutable
            player.removeMediaItem(index)
            _playbackState.update { it.copy(queue = mutable) }
        }
    }

    fun release() {
        progressTickerJob?.cancel()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
