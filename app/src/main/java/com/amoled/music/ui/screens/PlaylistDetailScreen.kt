package com.amoled.music.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.data.model.Playlist
import com.amoled.music.data.model.Song
import com.amoled.music.ui.components.SongItem
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledCardBackground
import com.amoled.music.ui.theme.AmoledCardBorder
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    songs: List<Song>,
    playbackState: PlaybackState,
    onBack: () -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSongClick: (index: Int) -> Unit,
    onPinToHome: () -> Unit,
    onEditPlaylist: () -> Unit,
    onDeletePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header with Banner and Gradient Overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.4f)
                        .background(AmoledCardBackground)
                ) {
                    if (!playlist.bannerUriString.isNullOrBlank()) {
                        AsyncImage(
                            model = playlist.bannerUriString,
                            contentDescription = playlist.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LibraryMusic,
                                contentDescription = null,
                                tint = AmoledSurfaceElevated,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    // Scrim gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.2f),
                                        AmoledBlack
                                    )
                                )
                            )
                    )

                    // Navigation bar on top of banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AmoledBlack.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = AmoledWhite
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Edit
                            IconButton(
                                onClick = onEditPlaylist,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AmoledBlack.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = AmoledWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Delete
                            IconButton(
                                onClick = onDeletePlaylist,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(AmoledBlack.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Playlist Info at bottom of banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = playlist.name,
                            color = AmoledWhite,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${songs.size} tracks",
                            color = AmoledTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Action Buttons Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Play All
                    Button(
                        onClick = onPlayAll,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledWhite,
                            contentColor = AmoledBlack
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AmoledBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Play All",
                            color = AmoledBlack,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    // Shuffle
                    OutlinedButton(
                        onClick = onShuffleAll,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
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

                    // Pin to Home Screen
                    Button(
                        onClick = onPinToHome,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmoledSurfaceElevated,
                            contentColor = AmoledWhite
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddHome,
                            contentDescription = null,
                            tint = AmoledWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Add to Home",
                            color = AmoledWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Song List
            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No songs in this playlist",
                                color = AmoledTextSecondary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onEditPlaylist,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AmoledWhite,
                                    contentColor = AmoledBlack
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Add Songs",
                                    color = AmoledBlack,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(songs, key = { index, song -> "${song.id}_$index" }) { index, song ->
                    val isCurrent = playbackState.currentSong?.id == song.id &&
                            playbackState.currentPlaylistId == playlist.id
                    SongItem(
                        song = song,
                        isCurrentTrack = isCurrent,
                        isPlaying = isCurrent && playbackState.isPlaying,
                        onClick = { onSongClick(index) }
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}
