package com.amoled.music.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amoled.music.ui.theme.AmoledSurfaceElevated
import com.amoled.music.ui.theme.AmoledWhite

@Composable
fun AmoledExpressiveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    maxHeight: Dp = 48.dp,
    minHeight: Dp = 6.dp,
    barWidth: Dp = 4.dp,
    barColor: Color = AmoledWhite
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer_transition")

    // Multi-phase animations for rhythmic expressive wave movement
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 620, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase3"
    )

    val phase4 by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase4"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(maxHeight)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = when (i % 4) {
                0 -> phase1
                1 -> phase2
                2 -> phase3
                else -> phase4
            }

            val envelope = kotlin.math.sin((i.toFloat() / barCount) * Math.PI).toFloat().coerceIn(0.35f, 1.0f)
            val currentFraction = if (isPlaying) (factor * envelope).coerceIn(0.12f, 1.0f) else 0.15f
            val currentBarHeight = minHeight + (maxHeight - minHeight) * currentFraction

            Box(
                modifier = Modifier
                    .width(barWidth)
                    .height(currentBarHeight)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(if (isPlaying) barColor else AmoledSurfaceElevated)
            )
        }
    }
}
