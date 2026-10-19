package com.amoled.music.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun `progress calculation handles normal playback progression`() {
        val state = PlaybackState(
            currentPositionMs = 30000L,
            totalDurationMs = 120000L,
            isPlaying = true
        )
        assertEquals(0.25f, state.progress, 0.001f)
    }

    @Test
    fun `progress calculation avoids division by zero when duration is zero or negative`() {
        val zeroState = PlaybackState(
            currentPositionMs = 5000L,
            totalDurationMs = 0L
        )
        assertEquals(0f, zeroState.progress, 0.0f)

        val negativeDurationState = PlaybackState(
            currentPositionMs = 5000L,
            totalDurationMs = -100L
        )
        assertEquals(0f, negativeDurationState.progress, 0.0f)
    }

    @Test
    fun `progress calculation coerces within bounds 0 to 1`() {
        val overflowState = PlaybackState(
            currentPositionMs = 250000L,
            totalDurationMs = 200000L
        )
        assertEquals(1.0f, overflowState.progress, 0.0f)

        val negativePosState = PlaybackState(
            currentPositionMs = -5000L,
            totalDurationMs = 200000L
        )
        assertEquals(0.0f, negativePosState.progress, 0.0f)
    }

    @Test
    fun `repeatMode enum transitions cleanly`() {
        assertEquals(RepeatMode.OFF, RepeatMode.valueOf("OFF"))
        assertEquals(RepeatMode.ALL, RepeatMode.valueOf("ALL"))
        assertEquals(RepeatMode.ONE, RepeatMode.valueOf("ONE"))
    }

    @Test
    fun `default playbackState is idle and uninitialized`() {
        val defaultState = PlaybackState()
        assertNull(defaultState.currentSong)
        assertFalse(defaultState.isPlaying)
        assertEquals(0L, defaultState.currentPositionMs)
        assertEquals(0L, defaultState.totalDurationMs)
        assertFalse(defaultState.isShuffleEnabled)
        assertEquals(RepeatMode.OFF, defaultState.repeatMode)
        assertTrue(defaultState.queue.isEmpty())
        assertEquals(-1, defaultState.currentTrackIndex)
    }
}
