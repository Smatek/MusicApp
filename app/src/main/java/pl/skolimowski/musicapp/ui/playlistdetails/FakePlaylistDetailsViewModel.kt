package pl.skolimowski.musicapp.ui.playlistdetails

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlaylistDetailsViewModel(
    initialState: PlaylistDetailsState = PlaylistDetailsState(),
) : PlaylistDetailsViewModelInterface {

    override val state: StateFlow<PlaylistDetailsState> = MutableStateFlow(initialState)

    override fun handleIntent(intent: PlaylistDetailsIntent) {
        // No-op for fake implementation, or add simple logic if needed for previews
    }
} 