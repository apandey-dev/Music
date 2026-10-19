package com.amoled.music.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SongTest {

    @Test
    fun `formattedDuration correctly formats standard track durations`() {
        val song1 = Song(
            id = 1L,
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            duration = 243000L, // 4 mins 3 seconds
            contentUriString = "content://media/external/audio/media/1"
        )
        assertEquals("04:03", song1.formattedDuration())

        val song2 = song1.copy(duration = 65000L) // 1 min 5 seconds
        assertEquals("01:05", song2.formattedDuration())

        val song3 = song1.copy(duration = 0L)
        assertEquals("00:00", song3.formattedDuration())

        val song4 = song1.copy(duration = 3600000L) // 60 mins 0 seconds
        assertEquals("60:00", song4.formattedDuration())
    }

    @Test
    fun `song copy and equality work as expected`() {
        val songA = Song(
            id = 100L,
            title = "Starboy",
            artist = "The Weeknd",
            album = "Starboy",
            duration = 230000L,
            contentUriString = "content://media/external/audio/media/100"
        )
        val songB = songA.copy()

        assertEquals(songA, songB)
        assertEquals(songA.hashCode(), songB.hashCode())

        val songC = songA.copy(id = 101L)
        assertNotEquals(songA, songC)
    }

    @Test
    fun `song optional fields default properly`() {
        val song = Song(
            id = 10L,
            title = "Track",
            artist = "Artist",
            album = "Album",
            duration = 1000L,
            contentUriString = "content://test/1"
        )
        assertNull(song.albumArtUriString)
        assertEquals("", song.dataPath)
    }
}
