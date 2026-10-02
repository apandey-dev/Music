package com.amoled.music.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.amoled.music.data.model.Playlist
import com.amoled.music.ui.theme.AmoledBlack
import com.amoled.music.ui.theme.AmoledCardBackground
import com.amoled.music.ui.theme.AmoledCardBorder
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledTextPrimary
import com.amoled.music.ui.theme.AmoledTextSecondary
import com.amoled.music.ui.theme.AmoledWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistOptionsBottomSheet(
    playlist: Playlist,
    onDismiss: () -> Unit,
    onPinToHome: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AmoledBlack,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = AmoledSurfaceElevated
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Playlist Header Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Banner Thumbnail
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmoledSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (!playlist.bannerUriString.isNullOrBlank()) {
                        AsyncImage(
                            model = playlist.bannerUriString,
                            contentDescription = playlist.name,
                            modifier = Modifier.size(52.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = AmoledTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        color = AmoledTextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlist.songCount} tracks",
                        color = AmoledTextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            HorizontalDivider(
                color = AmoledCardBorder,
                thickness = 0.8.dp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Option 1: Edit Playlist (Pencil in Circle)
            PlaylistOptionItem(
                icon = Icons.Default.Edit,
                iconTint = AmoledWhite,
                iconBgColor = AmoledSurfaceElevated,
                title = "Edit Group",
                subtitle = "Rename, change banner, or edit songs",
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 2: Pin to Home Screen
            PlaylistOptionItem(
                icon = Icons.Default.AddHome,
                iconTint = AmoledWhite,
                iconBgColor = AmoledSurfaceElevated,
                title = "Add to Home Screen",
                subtitle = "Place a quick shortcut on launcher",
                onClick = {
                    onDismiss()
                    onPinToHome()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Option 3: Delete Playlist (Circular Delete Sweep Icon)
            PlaylistOptionItem(
                icon = Icons.Outlined.DeleteSweep,
                iconTint = Color(0xFFFF453A),
                iconBgColor = Color(0xFF3B1515),
                title = "Delete Group",
                subtitle = "Remove this group permanently",
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun PlaylistOptionItem(
    icon: ImageVector,
    iconTint: Color,
    iconBgColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmoledCardBackground)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Icon Badge
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AmoledTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = AmoledTextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
