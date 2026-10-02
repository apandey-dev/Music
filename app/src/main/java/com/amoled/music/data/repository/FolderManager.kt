package com.amoled.music.data.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ScannedFolder(
    val uriString: String,
    val displayName: String,
    val songCount: Int = 0
) {
    val uri: Uri get() = Uri.parse(uriString)
}

class FolderManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("amoled_music_folders", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_FOLDERS_V2 = "saved_folder_models_v2"

    fun getCustomFolders(): List<ScannedFolder> {
        val json = prefs.getString(KEY_FOLDERS_V2, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ScannedFolder>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCustomFolderUris(): List<Uri> {
        return getCustomFolders().map { it.uri }
    }

    fun addCustomFolder(uri: Uri): ScannedFolder {
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (_: Exception) {}

        val docFile = DocumentFile.fromTreeUri(context, uri)
        val name = docFile?.name?.ifBlank { "Music Folder" } ?: "Music Folder"

        val current = getCustomFolders().toMutableList()
        current.removeAll { it.uriString == uri.toString() }
        val newFolder = ScannedFolder(uriString = uri.toString(), displayName = name)
        current.add(newFolder)
        saveFolders(current)
        return newFolder
    }

    fun removeCustomFolder(uriString: String) {
        val current = getCustomFolders().toMutableList()
        current.removeAll { it.uriString == uriString }
        saveFolders(current)
    }

    private fun saveFolders(folders: List<ScannedFolder>) {
        prefs.edit().putString(KEY_FOLDERS_V2, gson.toJson(folders)).apply()
    }
}
