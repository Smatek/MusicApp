package pl.skolimowski.musicapp

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcher
import pl.skolimowski.musicapp.player.MusicController
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val networkStateWatcher: NetworkStateWatcher,
    private val musicController: MusicController
) : ViewModel() {

    fun sendIntent(intent: MainActivityIntent) {
        when (intent) {
            is MainActivityIntent.RegisterNetworkCallback -> registerWatcher()
            is MainActivityIntent.UnregisterNetworkCallback -> unregisterWatcher()
            is MainActivityIntent.OnDestroy -> musicController.release()
        }
    }

    private fun registerWatcher() {
        networkStateWatcher.register()
    }

    private fun unregisterWatcher() {
        networkStateWatcher.unregister()
    }
} 