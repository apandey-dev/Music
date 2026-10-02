package com.amoled.music

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.Song
import com.amoled.music.ui.components.AddToPlaylistBottomSheet
import com.amoled.music.ui.components.FolderManagementBottomSheet
import com.amoled.music.ui.components.FullPlayerModal
import com.amoled.music.ui.components.MiniPlayer
import com.amoled.music.ui.components.QueueBottomSheet
import com.amoled.music.ui.components.SamsungTopTabBar
import com.amoled.music.ui.screens.AllTracksScreen
import com.amoled.music.ui.screens.HomeScreen
import com.amoled.music.ui.screens.PlaylistDetailScreen
import com.amoled.music.ui.screens.SongPickerScreen
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledMusicTheme
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        setContent {
            AmoledMusicTheme {
                MainAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && data.scheme == "amoledmusic" && data.host == "playlist") {
            val playlistId = data.getQueryParameter("id")?.toLongOrNull()
            val autoPlay = data.getQueryParameter("autoPlay")?.toBoolean() ?: false
            if (playlistId != null) {
                viewModel.loadPlaylist(playlistId)
                if (autoPlay) {
                    viewModel.playPlaylistById(playlistId)
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainAppContent(
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val playlists by viewModel.playlists.collectAsState()
    val deviceSongs by viewModel.deviceSongs.collectAsState()
    val selectedPlaylistWithSongs by viewModel.selectedPlaylistWithSongs.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    val scannedFolders by viewModel.scannedFolders.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isInitialLoading by viewModel.isInitialLoading.collectAsState()

    var showFullPlayer by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var showFolderSheet by remember { mutableStateOf(false) }
    var songForAddToPlaylist by remember { mutableStateOf<Song?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Intercept back button when Full Player is open: minimize it to MiniPlayer instead of closing app
    BackHandler(enabled = showFullPlayer) {
        showFullPlayer = false
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Permissions
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.POST_NOTIFICATIONS
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    val permissionsState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (permissionsState.allPermissionsGranted) {
                    viewModel.scanSongs(isManual = false)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.scanSongs(isManual = false)
        }
    }

    if (!permissionsState.allPermissionsGranted) {
        // Permission Request UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AmoledWhite,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Amoled Music",
                    color = AmoledTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Audio permission is needed to scan and play music stored on your device in pure AMOLED style.",
                    color = AmoledTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmoledWhite,
                        contentColor = AmoledBlack
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Grant Permission",
                        color = AmoledBlack,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Scaffold(
            containerColor = AmoledBlack,
            bottomBar = {
                // Bottom Mini Player pinned above navigation bars
                Column(
                    modifier = Modifier
                        .background(AmoledBlack)
                        .navigationBarsPadding()
                ) {
                    if (!showFullPlayer && playbackState.currentSong != null) {
                        MiniPlayer(
                            playbackState = playbackState,
                            onTogglePlay = { viewModel.playbackController.togglePlay() },
                            onSkipNext = { viewModel.playbackController.next() },
                            onClick = { showFullPlayer = true }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "main",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Main Samsung Style Swipeable Screen (Songs & Playlists HorizontalPager)
                composable("main") {
                    val pagerState = rememberPagerState(pageCount = { 2 })

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AmoledBlack)
                    ) {
                        // Samsung Style Top Scrollable TabRow
                        SamsungTopTabBar(
                            selectedTabIndex = pagerState.currentPage,
                            onTabSelected = { pageIndex ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pageIndex)
                                }
                            },
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            isSearching = isSearching,
                            onToggleSearch = {
                                isSearching = !isSearching
                                if (!isSearching) searchQuery = ""
                            },
                            isScanning = isScanning,
                            onManualScan = {
                                viewModel.scanSongs(isManual = true)
                                Toast.makeText(context, "Scanning songs...", Toast.LENGTH_SHORT).show()
                            },
                            onOpenFolderSheet = {
                                showFolderSheet = true
                            }
                        )

                        // Swipeable HorizontalPager
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) { page ->
                            if (page == 0) {
                                // 1. Songs Tab
                                AllTracksScreen(
                                    songs = deviceSongs,
                                    playbackState = playbackState,
                                    searchQuery = searchQuery,
                                    isScanning = isScanning,
                                    isInitialLoading = isInitialLoading,
                                    hasCustomFolders = scannedFolders.isNotEmpty(),
                                    onSongClick = { index, songs ->
                                        viewModel.playPlaylist(
                                            playlist = Playlist(name = "Songs", songCount = songs.size),
                                            songs = songs,
                                            startIndex = index
                                        )
                                    },
                                    onPlayAll = { songs ->
                                        viewModel.playPlaylist(
                                            playlist = Playlist(name = "Songs", songCount = songs.size),
                                            songs = songs,
                                            startIndex = 0
                                        )
                                    },
                                    onShuffleAll = { songs ->
                                        viewModel.playPlaylist(
                                            playlist = Playlist(name = "Songs", songCount = songs.size),
                                            songs = songs,
                                            startIndex = 0,
                                            shuffle = true
                                        )
                                    },
                                    onPlayNext = { song ->
                                        viewModel.playbackController.addSongToQueueNext(song)
                                        Toast.makeText(context, "Playing next: ${song.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    onAddToGroup = { song ->
                                        songForAddToPlaylist = song
                                    },
                                    onOpenFolderSheet = {
                                        showFolderSheet = true
                                    }
                                )
                            } else {
                                // 2. Playlists Tab
                                HomeScreen(
                                    playlists = playlists,
                                    searchQuery = searchQuery,
                                    onPlaylistClick = { playlist ->
                                        viewModel.loadPlaylist(playlist.id)
                                        navController.navigate("playlist_detail/${playlist.id}")
                                    },
                                    onPlayPlaylist = { playlist ->
                                        viewModel.loadPlaylist(playlist.id)
                                        viewModel.playPlaylistById(playlist.id)
                                    },
                                    onPinToHome = { playlist ->
                                        viewModel.pinPlaylistToHome(context, playlist)
                                    },
                                    onEditPlaylist = { playlist ->
                                        viewModel.loadPlaylist(playlist.id)
                                        navController.navigate("song_picker?playlistId=${playlist.id}")
                                    },
                                    onDeletePlaylist = { playlist ->
                                        viewModel.deletePlaylist(playlist.id) {
                                            Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onCreateNewClick = {
                                        navController.navigate("song_picker")
                                    }
                                )
                            }
                        }
                    }
                }

                // 3. Playlist Detail Screen
                composable(
                    route = "playlist_detail/{playlistId}",
                    arguments = listOf(navArgument("playlistId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    LaunchedEffect(playlistId) {
                        viewModel.loadPlaylist(playlistId)
                    }

                    val playlistData = selectedPlaylistWithSongs
                    if (playlistData != null) {
                        PlaylistDetailScreen(
                            playlist = playlistData.playlist,
                            songs = playlistData.songs,
                            playbackState = playbackState,
                            onBack = { navController.popBackStack() },
                            onPlayAll = {
                                viewModel.playPlaylist(
                                    playlist = playlistData.playlist,
                                    songs = playlistData.songs,
                                    startIndex = 0
                                )
                            },
                            onShuffleAll = {
                                viewModel.playPlaylist(
                                    playlist = playlistData.playlist,
                                    songs = playlistData.songs,
                                    startIndex = 0,
                                    shuffle = true
                                )
                            },
                            onSongClick = { index ->
                                viewModel.playPlaylist(
                                    playlist = playlistData.playlist,
                                    songs = playlistData.songs,
                                    startIndex = index
                                )
                            },
                            onPinToHome = {
                                viewModel.pinPlaylistToHome(context, playlistData.playlist)
                            },
                            onEditPlaylist = {
                                navController.navigate("song_picker?playlistId=${playlistData.playlist.id}")
                            },
                            onDeletePlaylist = {
                                viewModel.deletePlaylist(playlistData.playlist.id) {
                                    navController.popBackStack()
                                    Toast.makeText(context, "Playlist deleted", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // 4. Create / Edit Playlist (2-Step Flow)
                composable(
                    route = "song_picker?playlistId={playlistId}",
                    arguments = listOf(
                        navArgument("playlistId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        }
                    )
                ) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("playlistId") ?: 0L
                    val existing = selectedPlaylistWithSongs?.takeIf { it.playlist.id == playlistId }

                    SongPickerScreen(
                        initialName = existing?.playlist?.name ?: "",
                        initialBannerUri = existing?.playlist?.bannerUriString,
                        initialSelectedSongIds = existing?.songs?.map { it.id }?.toSet() ?: emptySet(),
                        allDeviceSongs = deviceSongs,
                        onBack = { navController.popBackStack() },
                        onSave = { name, bannerUri, selectedSongs ->
                            viewModel.savePlaylist(
                                playlistId = if (playlistId > 0) playlistId else null,
                                name = name,
                                bannerUri = bannerUri,
                                songs = selectedSongs
                            ) { savedId ->
                                navController.popBackStack()
                                if (playlistId == 0L) {
                                    navController.navigate("playlist_detail/$savedId")
                                }
                            }
                        }
                    )
                }
            }

            // Full Player Modal with Material 3 Expressive Audio Visualizer
            FullPlayerModal(
                visible = showFullPlayer,
                playbackState = playbackState,
                onDismiss = { showFullPlayer = false },
                onTogglePlay = { viewModel.playbackController.togglePlay() },
                onPrevious = { viewModel.playbackController.previous() },
                onNext = { viewModel.playbackController.next() },
                onSeekTo = { viewModel.playbackController.seekTo(it) },
                onToggleShuffle = { viewModel.playbackController.toggleShuffle() },
                onToggleRepeat = { viewModel.playbackController.toggleRepeat() },
                onOpenQueue = { showQueueSheet = true }
            )

            // Queue Bottom Sheet with Dual Swipe Actions
            if (showQueueSheet) {
                QueueBottomSheet(
                    playbackState = playbackState,
                    onDismissRequest = { showQueueSheet = false },
                    onSongClick = { index ->
                        viewModel.playbackController.playSongFromCurrentQueue(index)
                    },
                    onRemoveSong = { index ->
                        viewModel.playbackController.removeQueueItem(index)
                    },
                    onPlayNextSong = { index ->
                        viewModel.playbackController.playNext(index)
                        Toast.makeText(context, "Playing next", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Folder Management Bottom Sheet
            if (showFolderSheet) {
                FolderManagementBottomSheet(
                    folders = scannedFolders,
                    onDismissRequest = { showFolderSheet = false },
                    onAddFolder = { uri ->
                        viewModel.addCustomFolder(uri)
                        Toast.makeText(context, "Added folder to music scan", Toast.LENGTH_SHORT).show()
                    },
                    onRemoveFolder = { uriString ->
                        viewModel.removeCustomFolder(uriString)
                        Toast.makeText(context, "Folder removed", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Add to Playlist Bottom Sheet
            songForAddToPlaylist?.let { song ->
                AddToPlaylistBottomSheet(
                    song = song,
                    playlists = playlists,
                    onDismissRequest = { songForAddToPlaylist = null },
                    onPlaylistSelected = { targetPlaylist ->
                        viewModel.addSongToPlaylist(targetPlaylist.id, song) {
                            Toast.makeText(context, "Added to '${targetPlaylist.name}'", Toast.LENGTH_SHORT).show()
                            songForAddToPlaylist = null
                        }
                    }
                )
            }
        }
    }
}
