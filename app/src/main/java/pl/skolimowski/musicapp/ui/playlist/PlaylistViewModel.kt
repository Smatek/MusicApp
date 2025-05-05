package pl.skolimowski.musicapp.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.data.model.Playlist
import pl.skolimowski.musicapp.data.repository.PlaylistRepository
import pl.skolimowski.musicapp.data.repository.Result
import javax.inject.Inject

data class PlaylistState(
    val isLoading: Boolean = false,
    val trendingPlaylists: List<Playlist> = emptyList(),
    val error: String? = null
)

interface IPlaylistViewModel {
    val state: StateFlow<PlaylistState>
    fun handleIntent(intent: PlaylistIntent)
}

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel(), IPlaylistViewModel {

    private val _state = MutableStateFlow(PlaylistState())
    override val state: StateFlow<PlaylistState> = _state.asStateFlow()

    init {
        fetchPlaylists()
        observeTrendingPlaylists()
    }

    private fun fetchPlaylists() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = playlistRepository.fetchAndSaveTrendingPlaylists()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false, error = "Failed to fetch playlists: ${result.exception?.message ?: "Unknown error"}") }
                }
            }
        }
    }

    private fun observeTrendingPlaylists() {
        playlistRepository.observePlaylists()
            .onEach { playlists -> _state.update { it.copy(trendingPlaylists = playlists) } }
            .catch { throwable -> _state.update { it.copy(error = "Failed to observe playlists: ${throwable.message ?: "Unknown error"}") } }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: PlaylistIntent) {
        when (intent) {
            PlaylistIntent.Refresh -> fetchPlaylists()
            // Add other intent handling if needed
        }
    }
} 