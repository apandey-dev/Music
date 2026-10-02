package com.amoled.music

import android.app.Application
import com.amoled.music.data.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AmoledMusicApplication : Application() {

    lateinit var repository: MusicRepository
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = MusicRepository(this)

        // Restore playlists if app was reinstalled
        applicationScope.launch {
            repository.autoRestorePlaylists()
        }
    }
}
