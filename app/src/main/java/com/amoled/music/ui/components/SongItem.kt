package com.amoled.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isCurrentTrack: Boolean = false,
    isSelectable: Boolean = false,
    isSelected: Boolean = false,
    onSelectToggle: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit
) {
    val containerBg = when {
        isSelectable && isSelected -> AmoledSurfaceElevated
        isCurrentTrack -> AmoledSurfaceElevated
        else -> AmoledBlack
    }

    val borderModifier = if (isSelectable && isSelected) {
        Modifier.border(1.2.dp, AmoledWhite, RoundedCornerShape(8.dp))
    } else {
        Modifier.border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .clickable {
                if (isSelectable) {
                    onSelectToggle?.invoke(!isSelected)
                } else {
                    onClick()
                }
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Art / Placeholder
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
                color = if (isSelected || isCurrentTrack) AmoledWhite else AmoledTextPrimary,
                fontSize = 13.sp,
                fontWeight = if (isSelected || isCurrentTrack) FontWeight.Bold else FontWeight.Medium,
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
            color = if (isSelected) AmoledWhite else AmoledTextTertiary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
