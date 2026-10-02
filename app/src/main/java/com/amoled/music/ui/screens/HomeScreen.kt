package com.amoled.music.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amoled.music.data.model.Playlist
import com.amoled.music.ui.components.PlaylistCard
import com.amoled.music.ui.components.PlaylistOptionsBottomSheet
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite

@Composable
fun HomeScreen(
    playlists: List<Playlist>,
    searchQuery: String = "",
    onPlaylistClick: (Playlist) -> Unit,
    onPlayPlaylist: (Playlist) -> Unit,
    onPinToHome: (Playlist) -> Unit,
    onEditPlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onCreateNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlaylistForOptions by remember { mutableStateOf<Playlist?>(null) }

    val filteredPlaylists = remember(playlists, searchQuery) {
        if (searchQuery.isBlank()) {
            playlists
        } else {
            playlists.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Action Bar: "New Group" Button & Count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredPlaylists.size} groups",
                    color = AmoledTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = onCreateNewClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmoledWhite,
                        contentColor = AmoledBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "New Group",
                        color = AmoledBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Playlist Grid
            if (filteredPlaylists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = AmoledSurfaceElevated,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No Playlists Yet" else "No matching playlists",
                            color = AmoledTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create custom playlist groups with custom banners & pin them to home screen!",
                            color = AmoledTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onCreateNewClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledWhite,
                                contentColor = AmoledBlack
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "Create First Playlist",
                                color = AmoledBlack,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 4.dp,
                        bottom = 120.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPlaylists, key = { it.id }) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onCardClick = { onPlaylistClick(playlist) },
                            onPlayClick = { onPlayPlaylist(playlist) },
                            onOptionsClick = { selectedPlaylistForOptions = playlist }
                        )
                    }
                }
            }
        }

        // Custom Playlist Options Bottom Sheet
        selectedPlaylistForOptions?.let { playlist ->
            PlaylistOptionsBottomSheet(
                playlist = playlist,
                onDismiss = { selectedPlaylistForOptions = null },
                onPinToHome = { onPinToHome(playlist) },
                onEdit = { onEditPlaylist(playlist) },
                onDelete = { onDeletePlaylist(playlist) }
            )
        }
    }
}
