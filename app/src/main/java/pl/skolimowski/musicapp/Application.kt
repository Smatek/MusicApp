package pl.skolimowski.musicapp

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.player.MusicController
import javax.inject.Inject

@HiltAndroidApp
class Application: Application() {

    @Inject
    lateinit var musicController: MusicController

    override fun onCreate() {
        super.onCreate()

        // Connect the MusicController using the Process Lifecycle scope
        // This ensures the connection attempt starts when the app process starts
        // and uses a scope tied to the overall application process lifecycle.
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            musicController.connect()
        }
    }
}