package pl.skolimowski.musicapp.ui.playlist

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaylistViewModel(
    initialState: PlaylistState = PlaylistState()
) : IPlaylistViewModel {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<PlaylistState> = _state

    override fun handleIntent(intent: PlaylistIntent) {
        // No-op for fake implementation
    }
} 