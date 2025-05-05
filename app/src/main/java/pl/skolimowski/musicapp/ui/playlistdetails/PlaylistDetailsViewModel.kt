package pl.skolimowski.musicapp.ui.playlistdetails

import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.Playlist
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.repository.PlaylistRepository
import pl.skolimowski.musicapp.data.repository.Result
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.di.DispatcherProvider
import pl.skolimowski.musicapp.player.MusicController
import pl.skolimowski.musicapp.ui.navigation.AppRoutes.PlaylistDetailsRoute
import javax.inject.Inject

data class PlaylistDetailsState(
    val playlist: Playlist? = null,
    val tracksWithCacheStatus: Map<Track, CacheStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

interface PlaylistDetailsViewModelInterface {
    val state: StateFlow<PlaylistDetailsState>
    fun handleIntent(intent: PlaylistDetailsIntent)
}

@HiltViewModel
class PlaylistDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val trackRepository: TrackRepository,
    private val musicController: MusicController,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel(), PlaylistDetailsViewModelInterface {

    private val routeArgs: PlaylistDetailsRoute = savedStateHandle.toRoute()
    private val playlistId: String = routeArgs.playlistId

    private val _state = MutableStateFlow(PlaylistDetailsState(isLoading = true))
    override val state: StateFlow<PlaylistDetailsState> = combine(
        playlistRepository.observePlaylist(playlistId),
        trackRepository.getTrackCacheStatusFlow()
    ) { playlist, cacheStatusMap ->
        val tracksWithStatus = playlist?.tracks?.associateWith { track ->
            cacheStatusMap[track.streamUrl?.url?.toUri()] ?: CacheStatus.NOT_CACHED
        } ?: emptyMap()

        PlaylistDetailsState(
            playlist = playlist,
            tracksWithCacheStatus = tracksWithStatus,
            isLoading = false,
            error = _state.value.error
        )
    }.catch { e ->
        _state.update {
            it.copy(
                isLoading = false,
                error = e.message ?: "Failed to load playlist details"
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaylistDetailsState(isLoading = true)
    )

    init {
        fetchInitialTracks()
    }

    override fun handleIntent(intent: PlaylistDetailsIntent) {
        viewModelScope.launch {
            when (intent) {
                PlaylistDetailsIntent.PlayPlaylist -> loadPlaylistToController()
                else -> { /* No-op or handle other intents */
                }
            }
        }
    }

    private fun loadPlaylistToController() {
        val trackIds = state.value.playlist!!.tracks.map { it.trackInfo.id }

        viewModelScope.launch(dispatcherProvider.io) {
            var isFirstSet = false

            trackIds.forEach { trackId ->
                val track = trackRepository.getTrack(trackId)

                if (track is Result.Error) {
                    _state.update { it.copy(error = "Failed to load tracks") }
                    return@launch
                } else if (track is Result.Success) {
                    if (!isFirstSet) {
                        isFirstSet = true

                        withContext(dispatcherProvider.main) {
                            musicController.setTrack(track.data)
                            musicController.play()
                        }
                    } else {
                        withContext(dispatcherProvider.main) {
                            musicController.addTrack(track.data)
                        }
                    }
                }
            }
        }
    }

    private fun fetchInitialTracks() {
        viewModelScope.launch(dispatcherProvider.io) {
            _state.update { it.copy(isLoading = true, error = null) }

            when (val result = trackRepository.fetchAndSavePlaylistTracks(playlistId)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }

                    prefetchTracks(result.data)
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception?.message ?: "Failed to fetch tracks"
                        )
                    }
                }
            }
        }
    }

    private suspend fun prefetchTracks(trackInfos: List<TrackInfoEntity>) {
        trackRepository.prefetchTrackStart(trackInfos.map { it.id }.toSet())
    }
}