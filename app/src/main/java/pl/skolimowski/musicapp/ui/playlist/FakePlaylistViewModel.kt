package pl.skolimowski.musicapp.ui.playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaylistViewModel(
    initialState: PlaylistState = PlaylistState()
) : IPlaylistViewModel {

    override val state: StateFlow<PlaylistState> = MutableStateFlow(initialState)

    override fun handleIntent(intent: PlaylistIntent) {
        // No-op for fake implementation
    }
} 