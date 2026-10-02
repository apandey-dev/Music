package com.amoled.music.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.data.model.Song
import com.amoled.music.ui.components.SwipeableSongItem
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledCardBorder
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllTracksScreen(
    songs: List<Song>,
    playbackState: PlaybackState,
    searchQuery: String = "",
    isScanning: Boolean = false,
    isInitialLoading: Boolean = false,
    hasCustomFolders: Boolean = false,
    onSongClick: (index: Int, songs: List<Song>) -> Unit,
    onPlayAll: (songs: List<Song>) -> Unit,
    onShuffleAll: (songs: List<Song>) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToGroup: (Song) -> Unit,
    onOpenFolderSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Quick Actions: Play All & Shuffle
            if (filteredSongs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onPlayAll(filteredSongs) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledWhite,
                            contentColor = AmoledBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AmoledBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Play All",
                            color = AmoledBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    OutlinedButton(
                        onClick = { onShuffleAll(filteredSongs) },
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AmoledCardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = AmoledWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Shuffle",
                            color = AmoledWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = "${filteredSongs.size} tracks",
                        color = AmoledTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Song List
            if (filteredSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isInitialLoading) {
                        // Clean silent state during fast initial load (prevents any flash)
                        Box(modifier = Modifier.size(1.dp))
                    } else if (isScanning) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = AmoledWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Scanning device audio...",
                                color = AmoledTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = AmoledSurfaceElevated,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching songs" else if (hasCustomFolders) "No Music Found" else "No Songs Found",
                                color = AmoledTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "Try searching with a different term."
                                } else if (hasCustomFolders) {
                                    "No playable audio found in selected folders. Tap below to manage or add folders."
                                } else {
                                    "Choose a folder from your device to scan and play songs."
                                },
                                color = AmoledTextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenFolderSheet,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmoledWhite,
                                    contentColor = AmoledBlack
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (hasCustomFolders) "Manage Folders" else "Choose Music Folder",
                                    color = AmoledBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 120.dp)
                ) {
                    itemsIndexed(filteredSongs, key = { index, song -> "${song.id}_$index" }) { index, song ->
                        val isCurrentTrack = playbackState.currentSong?.id == song.id
                        SwipeableSongItem(
                            song = song,
                            isCurrentTrack = isCurrentTrack,
                            onPlayNext = { onPlayNext(song) },
                            onAddToGroup = { onAddToGroup(song) },
                            onClick = { onSongClick(index, filteredSongs) }
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                }
            }
        }
    }
}
