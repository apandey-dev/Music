package com.amoled.music.data.model

import android.net.Uri
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Song(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("album") val album: String,
    @SerializedName("duration") val duration: Long,
    @SerializedName("contentUriString") val contentUriString: String,
    @SerializedName("albumArtUriString") val albumArtUriString: String? = null,
    @SerializedName("dataPath") val dataPath: String = ""
) {
    val contentUri: Uri get() = Uri.parse(contentUriString)
    val albumArtUri: Uri? get() = albumArtUriString?.let { Uri.parse(it) }

    fun formattedDuration(): String {
        val totalSeconds = duration / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
