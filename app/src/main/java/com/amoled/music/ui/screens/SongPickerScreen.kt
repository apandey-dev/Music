package com.amoled.music.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amoled.music.data.model.Song
import com.amoled.music.data.repository.BannerStorageManager
import com.amoled.music.ui.components.AmoledTopBar
import com.amoled.music.ui.components.BannerPicker
import com.amoled.music.ui.components.SongItem
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledCardBackground
import com.amoled.music.ui.theme.AmoledCardBorder
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite
import kotlinx.coroutines.launch

@Composable
fun SongPickerScreen(
    initialName: String = "",
    initialBannerUri: String? = null,
    initialSelectedSongIds: Set<Long> = emptySet(),
    allDeviceSongs: List<Song>,
    onBack: () -> Unit,
    onSave: (name: String, bannerUri: String?, selectedSongs: List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(1) } // Step 1: Info (Name & Banner), Step 2: Songs Chooser

    var playlistName by remember { mutableStateOf(initialName) }
    var bannerUri by remember { mutableStateOf(initialBannerUri) }
    var selectedIds by remember { mutableStateOf(initialSelectedSongIds.toMutableSet()) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSongs = remember(allDeviceSongs, searchQuery) {
        if (searchQuery.isBlank()) {
            allDeviceSongs
        } else {
            allDeviceSongs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val handleFinalSave = {
        val name = playlistName.trim().ifEmpty { "My Playlist" }
        val selectedSongs = allDeviceSongs.filter { selectedIds.contains(it.id) }
        if (selectedSongs.isEmpty()) {
            Toast.makeText(context, "Please select at least 1 song", Toast.LENGTH_SHORT).show()
        } else {
            onSave(name, bannerUri, selectedSongs)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBlack)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            AmoledTopBar(
                title = if (currentStep == 1) {
                    if (initialName.isEmpty()) "New Playlist" else "Edit Details"
                } else {
                    "Choose Songs"
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (currentStep == 2) {
                                currentStep = 1
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AmoledWhite
                        )
                    }
                },
                actions = {
                    if (currentStep == 1) {
                        Button(
                            onClick = {
                                if (playlistName.isBlank()) {
                                    Toast.makeText(context, "Please enter a playlist name", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentStep = 2
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledWhite,
                                contentColor = AmoledBlack
                            ),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Next",
                                color = AmoledBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = AmoledBlack,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(start = 2.dp)
                            )
                        }
                    } else {
                        // Single Save Button on Step 2 (Text-only without tick icon)
                        Button(
                            onClick = handleFinalSave,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmoledWhite,
                                contentColor = AmoledBlack
                            ),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Save (${selectedIds.size})",
                                color = AmoledBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "PlaylistStepTransition"
            ) { step ->
                if (step == 1) {
                    // STEP 1: Name & Banner Page
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "PLAYLIST NAME",
                            color = AmoledTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = { playlistName = it },
                            placeholder = {
                                Text(
                                    "Enter group or playlist name...",
                                    color = AmoledTextSecondary,
                                    fontSize = 13.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AmoledCardBackground,
                                unfocusedContainerColor = AmoledCardBackground,
                                focusedBorderColor = AmoledWhite,
                                unfocusedBorderColor = AmoledCardBorder,
                                focusedTextColor = AmoledTextPrimary,
                                unfocusedTextColor = AmoledTextPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        BannerPicker(
                            bannerUriString = bannerUri,
                            onBannerSelected = { uriString ->
                                if (uriString != null) {
                                    coroutineScope.launch {
                                        val localUri = BannerStorageManager.saveBannerLocally(context, Uri.parse(uriString))
                                        bannerUri = localUri
                                    }
                                } else {
                                    bannerUri = null
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        Text(
                            text = "Tap 'Next' on the top right to select songs for this group.",
                            color = AmoledTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    // STEP 2: Full Page Song Chooser
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Search songs to add...",
                                    color = AmoledTextSecondary,
                                    fontSize = 12.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AmoledTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = AmoledTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = AmoledCardBackground,
                                unfocusedContainerColor = AmoledCardBackground,
                                focusedBorderColor = AmoledWhite,
                                unfocusedBorderColor = AmoledCardBorder,
                                focusedTextColor = AmoledTextPrimary,
                                unfocusedTextColor = AmoledTextPrimary
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )

                        // Quick Select All / Clear Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedIds.size} / ${filteredSongs.size} selected",
                                color = AmoledTextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AmoledSurfaceElevated)
                                        .clickable {
                                            val newSet = selectedIds.toMutableSet()
                                            filteredSongs.forEach { newSet.add(it.id) }
                                            selectedIds = newSet
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "All",
                                        color = AmoledWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(AmoledSurfaceElevated)
                                        .clickable {
                                            val newSet = selectedIds.toMutableSet()
                                            filteredSongs.forEach { newSet.remove(it.id) }
                                            selectedIds = newSet
                                        }
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Clear",
                                        color = AmoledTextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Song list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
                        ) {
                            items(filteredSongs, key = { it.id }) { song ->
                                val isSelected = selectedIds.contains(song.id)
                                SongItem(
                                    song = song,
                                    isSelectable = true,
                                    isSelected = isSelected,
                                    onSelectToggle = { checked ->
                                        val set = selectedIds.toMutableSet()
                                        if (checked) set.add(song.id) else set.remove(song.id)
                                        selectedIds = set
                                    },
                                    onClick = {
                                        val set = selectedIds.toMutableSet()
                                        if (isSelected) set.remove(song.id) else set.add(song.id)
                                        selectedIds = set
                                    }
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
