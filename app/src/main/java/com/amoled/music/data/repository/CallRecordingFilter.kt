package com.amoled.music.data.repository

import java.util.Locale

object CallRecordingFilter {

    private val excludedKeywords = listOf(
        "call_rec", "callrec", "call_record", "callrecording",
        "call recording", "voice call", "phonerecord", "phone_record",
        "call_sound", "sound_recorder/call", "recordings/call",
        "record/call", "dialer", "truecaller", "acrCalls", "auto_call"
    )

    fun isCallRecording(title: String?, dataPath: String?, displayName: String?): Boolean {
        val checkString = "${title ?: ""} ${dataPath ?: ""} ${displayName ?: ""}".lowercase(Locale.ROOT)

        for (kw in excludedKeywords) {
            if (checkString.contains(kw)) {
                return true
            }
        }

        // Check if file name matches typical phone number call recording pattern:
        // e.g. +919876543210_2023... or Call_9876543210 or 0987654321_
        val name = (displayName ?: title ?: "").lowercase(Locale.ROOT)
        if (name.startsWith("call_") || name.startsWith("call-") || name.startsWith("rec_call")) {
            return true
        }

        return false
    }
}
