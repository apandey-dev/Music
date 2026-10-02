package com.amoled.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueBottomSheet(
    playbackState: PlaybackState,
    onDismissRequest: () -> Unit,
    onSongClick: (Int) -> Unit,
    onRemoveSong: (Int) -> Unit,
    onPlayNextSong: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AmoledBlack,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AmoledSurfaceElevated)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            // Header (No X icon)
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "Current Queue (${playbackState.queue.size})",
                    color = AmoledTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Slide right to Play Next • Slide left to Remove",
                    color = AmoledTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(playbackState.queue, key = { index, song -> "${song.id}_$index" }) { index, song ->
                    val isCurrent = index == playbackState.currentTrackIndex

                    SwipeableSongItem(
                        song = song,
                        isCurrentTrack = isCurrent,
                        isPlaying = isCurrent && playbackState.isPlaying,
                        onPlayNext = { onPlayNextSong(index) },
                        onRemoveFromQueue = { onRemoveSong(index) },
                        onClick = { onSongClick(index) }
                    )
                }
            }
        }
    }
}
