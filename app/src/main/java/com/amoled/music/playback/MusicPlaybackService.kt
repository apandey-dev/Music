package com.amoled.music.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.amoled.music.AmoledMusicApplication
import com.amoled.music.MainActivity
import com.amoled.music.R
import com.amoled.music.widget.PlaylistWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val CHANNEL_ID = "music_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .build()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                updateWidgets()
                updateNotification()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidgets()
                updateNotification()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateWidgets()
                updateNotification()
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, buildNotification())
        }

        when (intent?.action) {
            PlaylistWidgetProvider.ACTION_TOGGLE_PLAY -> handleTogglePlay()
            PlaylistWidgetProvider.ACTION_NEXT_TRACK -> handleNext()
            PlaylistWidgetProvider.ACTION_PREV_TRACK -> handlePrevious()
            PlaylistWidgetProvider.ACTION_PLAY_PLAYLIST -> {
                val playlistId = intent.getLongExtra(PlaylistWidgetProvider.EXTRA_PLAYLIST_ID, -1L)
                handlePlayPlaylist(playlistId)
            }
        }

        return START_STICKY
    }

    private fun handleTogglePlay() {
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_IDLE || player.mediaItemCount == 0) {
                serviceScope.launch(Dispatchers.IO) {
                    val app = application as? AmoledMusicApplication
                    val songs = app?.repository?.getDeviceSongs() ?: emptyList()
                    if (songs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            val mediaItems = songs.map { song ->
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
                                MediaItem.Builder()
                                    .setUri(song.contentUri)
                                    .setMediaId(song.id.toString())
                                    .setMediaMetadata(metadata)
                                    .build()
                            }
                            player.setMediaItems(mediaItems, 0, 0L)
                            player.prepare()
                            player.play()
                            updateWidgets()
                            updateNotification()
                        }
                    }
                }
            } else {
                player.play()
            }
        }
        updateWidgets()
        updateNotification()
    }

    private fun handleNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        } else if (player.mediaItemCount > 0) {
            player.seekTo(0, 0L)
            player.play()
        }
        updateWidgets()
        updateNotification()
    }

    private fun handlePrevious() {
        if (player.currentPosition > 3000) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
        player.play()
        updateWidgets()
        updateNotification()
    }

    private fun handlePlayPlaylist(playlistId: Long) {
        serviceScope.launch(Dispatchers.IO) {
            val app = application as? AmoledMusicApplication ?: return@launch
            val playlist = if (playlistId > 0L) app.repository.getPlaylistById(playlistId) else null
            val songs = if (playlistId > 0L) app.repository.getSongsForPlaylist(playlistId) else app.repository.getDeviceSongs()

            val targetSongs = if (songs.isNotEmpty()) songs else app.repository.getDeviceSongs()
            if (targetSongs.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    val mediaItems = targetSongs.map { song ->
                        val extras = Bundle().apply {
                            putLong("song_id", song.id)
                            putString("data_path", song.dataPath)
                            putLong("duration", song.duration)
                            if (playlist != null) {
                                putString("playlist_name", playlist.name)
                                putLong("playlist_id", playlist.id)
                            }
                        }
                        val metadata = MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setAlbumTitle(song.album)
                            .setArtworkUri(song.albumArtUri ?: playlist?.bannerUriString?.let { Uri.parse(it) })
                            .setExtras(extras)
                            .build()
                        MediaItem.Builder()
                            .setUri(song.contentUri)
                            .setMediaId(song.id.toString())
                            .setMediaMetadata(metadata)
                            .build()
                    }
                    player.setMediaItems(mediaItems, 0, 0L)
                    player.prepare()
                    player.play()
                    updateWidgets()
                    updateNotification()
                }
            }
        }
    }

    private fun updateWidgets() {
        val currentItem = player.currentMediaItem
        val title = currentItem?.mediaMetadata?.title?.toString() ?: "Music"
        val artist = currentItem?.mediaMetadata?.artist?.toString() ?: "Tap to play"
        val playlistName = currentItem?.mediaMetadata?.extras?.getString("playlist_name")
        val subtitle = if (!playlistName.isNullOrBlank()) "$artist • $playlistName" else artist
        val artworkUri = currentItem?.mediaMetadata?.artworkUri?.toString()

        PlaylistWidgetProvider.updateAllWidgets(
            context = this,
            title = title,
            subtitle = subtitle,
            bannerUriString = artworkUri,
            isPlaying = player.isPlaying
        )
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val currentItem = player.currentMediaItem
        val title = currentItem?.mediaMetadata?.title?.toString() ?: "Music"
        val artist = currentItem?.mediaMetadata?.artist?.toString() ?: "Ready to play"

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, PlaylistWidgetProvider::class.java).apply {
            action = PlaylistWidgetProvider.ACTION_TOGGLE_PLAY
        }
        val togglePending = PendingIntent.getBroadcast(
            this, 10, toggleIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextIntent = Intent(this, PlaylistWidgetProvider::class.java).apply {
            action = PlaylistWidgetProvider.ACTION_NEXT_TRACK
        }
        val nextPending = PendingIntent.getBroadcast(
            this, 11, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevIntent = Intent(this, PlaylistWidgetProvider::class.java).apply {
            action = PlaylistWidgetProvider.ACTION_PREV_TRACK
        }
        val prevPending = PendingIntent.getBroadcast(
            this, 12, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(title)
            .setContentText(artist)
            .setContentIntent(openAppIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(player.isPlaying)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevPending)
            .addAction(
                if (player.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (player.isPlaying) "Pause" else "Play",
                togglePending
            )
            .addAction(android.R.drawable.ic_media_next, "Next", nextPending)

        mediaSession?.let { session ->
            builder.setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(0, 1, 2)
            )
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music Player foreground playback notification"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (::player.isInitialized) {
            player.pause()
            player.stop()
            player.clearMediaItems()
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
        updateWidgets()
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        if (::player.isInitialized) {
            player.stop()
            player.release()
        }
        mediaSession?.run {
            release()
            mediaSession = null
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(NOTIFICATION_ID)
        super.onDestroy()
    }
}
