package com.amoled.music.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amoled.music.data.model.Song
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledCardBackground
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledTextTertiary
import com.amoled.music.ui.theme.AmoledWhite
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableSongItem(
    song: Song,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isCurrentTrack: Boolean = false,
    onPlayNext: (() -> Unit)? = null,
    onAddToGroup: (() -> Unit)? = null,
    onRemoveFromQueue: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val maxDragPx = with(density) { 125.dp.toPx() } // Generous span so full label is visible without clipping
    val triggerThresholdPx = with(density) { 70.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(AmoledBlack)
    ) {
        // Swipe Action Background Revealed Underneath
        val currentOffset = offsetX.value

        if (currentOffset > 10f) {
            // Left to Right: Play Next (Full Height)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1B5E20))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Play Next",
                    color = AmoledWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        } else if (currentOffset < -10f) {
            // Right to Left: Add to Group or Remove (Full Height)
            val isQueueRemove = onRemoveFromQueue != null
            val bgColor = if (isQueueRemove) Color(0xFF8B0000) else AmoledSurfaceElevated
            val labelText = if (isQueueRemove) "Remove" else "Add to Group"

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = labelText,
                    color = AmoledWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Foreground Song Item Tile (Draggable with spring-back animation)
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isCurrentTrack) AmoledSurfaceElevated else AmoledBlack)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(-maxDragPx, maxDragPx)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value >= triggerThresholdPx) {
                                onPlayNext?.invoke()
                            } else if (offsetX.value <= -triggerThresholdPx) {
                                if (onRemoveFromQueue != null) {
                                    onRemoveFromQueue.invoke()
                                } else {
                                    onAddToGroup?.invoke()
                                }
                            }
                            // Smooth spring back to original position
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                        }
                    }
                )
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art Thumbnail
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(AmoledCardBackground),
                contentAlignment = Alignment.Center
            ) {
                if (song.albumArtUriString != null) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = AmoledTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (isCurrentTrack) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = AmoledWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Artist
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = if (isCurrentTrack) AmoledWhite else AmoledTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = AmoledTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Duration
            Text(
                text = song.formattedDuration(),
                color = AmoledTextTertiary,
                fontSize = 11.sp
            )
        }
    }
}
