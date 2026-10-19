package com.amoled.music.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallRecordingFilterTest {

    @Test
    fun `isCallRecording identifies files matching call recording keywords in title`() {
        assertTrue(CallRecordingFilter.isCallRecording("call_rec_20231010", "/music/audio.mp3", "audio.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("voice call with Alice", "/music/audio.mp3", "audio.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("Call Recording 101", "/music/audio.mp3", "audio.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("phonerecord_incoming", "/music/audio.mp3", "audio.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("truecaller_recording", "/music/audio.mp3", "audio.mp3"))
    }

    @Test
    fun `isCallRecording identifies call recording keywords in directory path`() {
        assertTrue(CallRecordingFilter.isCallRecording("Unknown Audio", "/storage/emulated/0/Recordings/call/rec1.m4a", "rec1.m4a"))
        assertTrue(CallRecordingFilter.isCallRecording("Audio 2", "/sdcard/sound_recorder/call/sample.aac", "sample.aac"))
        assertTrue(CallRecordingFilter.isCallRecording("Voice Clip", "/storage/emulated/0/dialer/recordings/audio.mp3", "audio.mp3"))
    }

    @Test
    fun `isCallRecording identifies files with phone call prefix patterns`() {
        assertTrue(CallRecordingFilter.isCallRecording("call_9876543210", "/music/call_9876543210.mp3", "call_9876543210.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("call-9876543210", "/music/call-9876543210.mp3", "call-9876543210.mp3"))
        assertTrue(CallRecordingFilter.isCallRecording("rec_call_01", "/music/rec_call_01.mp3", "rec_call_01.mp3"))
    }

    @Test
    fun `isCallRecording does not filter legitimate commercial songs with call in title`() {
        assertFalse(CallRecordingFilter.isCallRecording("Call Me Maybe", "/storage/emulated/0/Music/carly.mp3", "carly.mp3"))
        assertFalse(CallRecordingFilter.isCallRecording("Calling All Angels", "/storage/emulated/0/Music/train.mp3", "train.mp3"))
        assertFalse(CallRecordingFilter.isCallRecording("London Calling", "/storage/emulated/0/Music/clash.mp3", "clash.mp3"))
        assertFalse(CallRecordingFilter.isCallRecording("One Call Away", "/storage/emulated/0/Music/charlie.mp3", "charlie.mp3"))
        assertFalse(CallRecordingFilter.isCallRecording("Call Out My Name", "/storage/emulated/0/Music/theweeknd.mp3", "theweeknd.mp3"))
    }

    @Test
    fun `isCallRecording handles null or empty arguments gracefully`() {
        assertFalse(CallRecordingFilter.isCallRecording(null, null, null))
        assertFalse(CallRecordingFilter.isCallRecording("", "", ""))
        assertFalse(CallRecordingFilter.isCallRecording(null, "/valid/music/song.mp3", "song.mp3"))
    }
}
