package com.amoled.music

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.PlaylistWithSongs
import com.amoled.music.data.model.Song
import com.amoled.music.playback.PlaybackController
import com.amoled.music.widget.PlaylistWidgetProvider
import com.amoled.music.widget.ShortcutHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AmoledMusicApplication).repository
    val playbackController = PlaybackController(application)

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylistsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _deviceSongs = MutableStateFlow<List<Song>>(emptyList())
    val deviceSongs: StateFlow<List<Song>> = _deviceSongs.asStateFlow()

    private val _selectedPlaylistWithSongs = MutableStateFlow<PlaylistWithSongs?>(null)
    val selectedPlaylistWithSongs: StateFlow<PlaylistWithSongs?> = _selectedPlaylistWithSongs.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = playbackController.playbackState

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(true)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _scannedFolders = MutableStateFlow<List<com.amoled.music.data.repository.ScannedFolder>>(
        repository.folderManager.getCustomFolders()
    )
    val scannedFolders: StateFlow<List<com.amoled.music.data.repository.ScannedFolder>> = _scannedFolders.asStateFlow()

    init {
        loadFolders()
        scanSongs(isManual = false)
        observePlaybackForWidgets()
    }

    private fun loadFolders() {
        _scannedFolders.value = repository.folderManager.getCustomFolders()
    }

    private fun observePlaybackForWidgets() {
        viewModelScope.launch {
            playbackState.collect { state ->
                val song = state.currentSong
                val title = song?.title ?: "Music"
                val subtitle = if (song != null) "${song.artist} • ${state.currentPlaylistName}" else "Tap to play"
                PlaylistWidgetProvider.updateAllWidgets(
                    context = getApplication(),
                    title = title,
                    subtitle = subtitle,
                    bannerUriString = song?.albumArtUriString,
                    isPlaying = state.isPlaying
                )
            }
        }
    }

    fun scanSongs(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                _isScanning.value = true
            }
            loadFolders()
            _deviceSongs.value = repository.getDeviceSongs()
            _isInitialLoading.value = false
            if (isManual) {
                _isScanning.value = false
            }
        }
    }

    fun addCustomFolder(uri: android.net.Uri) {
        viewModelScope.launch {
            _isScanning.value = true
            _deviceSongs.value = repository.addMusicFolder(uri)
            loadFolders()
            _isScanning.value = false
        }
    }

    fun removeCustomFolder(uriString: String) {
        viewModelScope.launch {
            repository.folderManager.removeCustomFolder(uriString)
            loadFolders()
            scanSongs()
        }
    }

    fun addSongToPlaylist(playlistId: Long, song: Song, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
            onComplete()
        }
    }

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            _selectedPlaylistWithSongs.value = repository.getPlaylistWithSongs(playlistId)
        }
    }

    fun savePlaylist(
        playlistId: Long?,
        name: String,
        bannerUri: String?,
        songs: List<Song>,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = if (playlistId == null || playlistId == 0L) {
                repository.createPlaylist(name, bannerUri, songs)
            } else {
                repository.updatePlaylist(playlistId, name, bannerUri, songs)
                playlistId
            }
            loadPlaylist(id)
            onComplete(id)
        }
    }

    fun deletePlaylist(playlistId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            if (_selectedPlaylistWithSongs.value?.playlist?.id == playlistId) {
                _selectedPlaylistWithSongs.value = null
            }
            onComplete()
        }
    }

    fun playPlaylist(playlist: Playlist, songs: List<Song>, startIndex: Int = 0, shuffle: Boolean = false) {
        val playlistSongs = if (shuffle) songs.shuffled() else songs
        val index = if (shuffle) 0 else startIndex
        playbackController.playPlaylist(
            playlist = playlist,
            songs = playlistSongs,
            startIndex = index,
            startPlaying = true
        )
    }

    fun playPlaylistById(playlistId: Long, startIndex: Int = 0) {
        viewModelScope.launch {
            val playlistWithSongs = repository.getPlaylistWithSongs(playlistId)
            if (playlistWithSongs != null && playlistWithSongs.songs.isNotEmpty()) {
                _selectedPlaylistWithSongs.value = playlistWithSongs
                playPlaylist(playlistWithSongs.playlist, playlistWithSongs.songs, startIndex)
            }
        }
    }

    fun pinPlaylistToHome(context: Context, playlist: Playlist) {
        viewModelScope.launch {
            ShortcutHelper.pinPlaylistToHome(context, playlist)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playbackController.release()
    }
}
