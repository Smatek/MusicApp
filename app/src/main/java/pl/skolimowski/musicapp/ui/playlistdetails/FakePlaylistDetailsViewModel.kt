package pl.skolimowski.musicapp.ui.playlistdetails

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.player.MusicController

class FakePlaylistDetailsViewModel(
    initialState: PlaylistDetailsState = PlaylistDetailsState(),
    initialTracks: Map<Track, CacheStatus> = emptyMap(), // Allow setting initial tracks
    private val musicControllerMock: MusicController? = null // Accept optional mock
) : PlaylistDetailsViewModelInterface {

    private val _state = MutableStateFlow(initialState.copy(tracksWithCacheStatus = initialTracks))
    override val state: StateFlow<PlaylistDetailsState> = _state.asStateFlow()

    override fun handleIntent(intent: PlaylistDetailsIntent) {
        // No-op for fake implementation, or add simple logic if needed for previews
    }
} 