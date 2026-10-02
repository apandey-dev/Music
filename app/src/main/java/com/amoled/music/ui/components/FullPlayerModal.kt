package com.amoled.music.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amoled.music.data.model.PlaybackState
import com.amoled.music.data.model.RepeatMode
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledTextTertiary
import com.amoled.music.ui.theme.AmoledWhite

// Material 3 Expressive Fluid Banner Shape (Ultra smooth organic curve matching reference)
val ExpressiveBannerShape = RoundedCornerShape(
    topStart = 44.dp,
    topEnd = 36.dp,
    bottomEnd = 44.dp,
    bottomStart = 38.dp
)

// Center Play/Pause Organic Blob Shape
val ExpressiveCenterBlobShape = RoundedCornerShape(
    topStart = 38.dp,
    topEnd = 24.dp,
    bottomEnd = 38.dp,
    bottomStart = 28.dp
)

// Secondary Organic Buttons Shape
val ExpressiveButtonShape = RoundedCornerShape(22.dp)

// Palette matching reference image
val ExpressiveLavender = Color(0xFFD4C2FC)
val ExpressiveDarkText = Color(0xFF16151E)
val ExpressiveButtonBg = Color(0xFF1C1C24)
val ExpressiveButtonBorder = Color(0xFF2E2E3C)
val ExpressiveTrackInactive = Color(0xFF22222E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerModal(
    visible: Boolean,
    playbackState: PlaybackState,
    onDismiss: () -> Unit,
    onTogglePlay: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onOpenQueue: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(animationSpec = tween(250))
    ) {
        val song = playbackState.currentSong ?: return@AnimatedVisibility

        var isDraggingSlider by remember { mutableStateOf(false) }
        var sliderPosition by remember { mutableFloatStateOf(0f) }

        val currentPositionMs = if (isDraggingSlider) {
            (sliderPosition * playbackState.totalDurationMs).toLong()
        } else {
            playbackState.currentPositionMs
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
                .statusBarsPadding()
                .navigationBarsPadding()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {}
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Collapse",
                            tint = AmoledWhite,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NOW PLAYING",
                            color = AmoledTextTertiary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = playbackState.currentPlaylistName.ifEmpty { "Songs" },
                            color = AmoledTextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = onOpenQueue,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = AmoledWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // High-End Expressive Organic Fluid Banner Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .aspectRatio(1.0f)
                        .shadow(
                            elevation = 18.dp,
                            shape = ExpressiveBannerShape,
                            spotColor = Color(0x66000000),
                            ambientColor = Color(0x33000000)
                        )
                        .clip(ExpressiveBannerShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF2C243B), Color(0xFF181522), AmoledBlack)
                            )
                        )
                        .border(1.dp, Color(0x22FFFFFF), ExpressiveBannerShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (song.albumArtUriString != null) {
                        AsyncImage(
                            model = song.albumArtUri,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Default Aesthetic Music Wallpaper with subtle deep sunset twilight gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF4A3563),
                                            Color(0xFF281E3A),
                                            Color(0xFF14121C)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = ExpressiveLavender.copy(alpha = 0.85f),
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Track Title & Subtitle (Clean & Large)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = song.title,
                        color = AmoledWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (song.artist.isNotBlank()) song.artist else "Unknown Artist",
                        color = AmoledTextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar & Dual Timestamps
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp)
                ) {
                    Slider(
                        value = if (isDraggingSlider) sliderPosition else playbackState.progress,
                        onValueChange = {
                            isDraggingSlider = true
                            sliderPosition = it
                        },
                        onValueChangeFinished = {
                            val newPos = (sliderPosition * playbackState.totalDurationMs).toLong()
                            onSeekTo(newPos)
                            isDraggingSlider = false
                        },
                        thumb = {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(ExpressiveLavender)
                                    .shadow(4.dp, CircleShape)
                            )
                        },
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                modifier = Modifier.height(4.dp),
                                colors = SliderDefaults.colors(
                                    activeTrackColor = ExpressiveLavender,
                                    inactiveTrackColor = ExpressiveTrackInactive
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(currentPositionMs),
                            color = AmoledTextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = formatTime(playbackState.totalDurationMs),
                            color = AmoledTextTertiary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5 Expressive Organic Controls Row with Active State Highlighting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isShuffle = playbackState.isShuffleEnabled
                    val isRepeatActive = playbackState.repeatMode != RepeatMode.OFF

                    // 1. Shuffle Button (Active Highlight)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = if (isShuffle) 8.dp else 0.dp,
                                shape = ExpressiveButtonShape,
                                spotColor = ExpressiveLavender.copy(alpha = 0.5f)
                            )
                            .clip(ExpressiveButtonShape)
                            .background(if (isShuffle) ExpressiveLavender else ExpressiveButtonBg)
                            .border(
                                width = 1.dp,
                                color = if (isShuffle) ExpressiveLavender else ExpressiveButtonBorder,
                                shape = ExpressiveButtonShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(
                                    bounded = true,
                                    color = if (isShuffle) ExpressiveDarkText else AmoledWhite.copy(alpha = 0.2f)
                                )
                            ) { onToggleShuffle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (isShuffle) ExpressiveDarkText else AmoledTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // 2. Previous Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(ExpressiveButtonShape)
                            .background(ExpressiveButtonBg)
                            .border(1.dp, ExpressiveButtonBorder, ExpressiveButtonShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true, color = AmoledWhite.copy(alpha = 0.2f))
                            ) { onPrevious() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            tint = AmoledWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 3. Center Play / Pause Expressive Blob Button
                    Box(
                        modifier = Modifier
                            .size(width = 76.dp, height = 72.dp)
                            .shadow(
                                elevation = 14.dp,
                                shape = ExpressiveCenterBlobShape,
                                spotColor = ExpressiveLavender.copy(alpha = 0.5f),
                                ambientColor = Color(0x33000000)
                            )
                            .clip(ExpressiveCenterBlobShape)
                            .background(ExpressiveLavender)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true, color = ExpressiveDarkText)
                            ) { onTogglePlay() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playbackState.isPlaying) "Pause" else "Play",
                            tint = ExpressiveDarkText,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // 4. Next Button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(ExpressiveButtonShape)
                            .background(ExpressiveButtonBg)
                            .border(1.dp, ExpressiveButtonBorder, ExpressiveButtonShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true, color = AmoledWhite.copy(alpha = 0.2f))
                            ) { onNext() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next",
                            tint = AmoledWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 5. Repeat Button (Active Highlight)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = if (isRepeatActive) 8.dp else 0.dp,
                                shape = ExpressiveButtonShape,
                                spotColor = ExpressiveLavender.copy(alpha = 0.5f)
                            )
                            .clip(ExpressiveButtonShape)
                            .background(if (isRepeatActive) ExpressiveLavender else ExpressiveButtonBg)
                            .border(
                                width = 1.dp,
                                color = if (isRepeatActive) ExpressiveLavender else ExpressiveButtonBorder,
                                shape = ExpressiveButtonShape
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(
                                    bounded = true,
                                    color = if (isRepeatActive) ExpressiveDarkText else AmoledWhite.copy(alpha = 0.2f)
                                )
                            ) { onToggleRepeat() },
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (playbackState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Repeat",
                            tint = if (isRepeatActive) ExpressiveDarkText else AmoledTextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
