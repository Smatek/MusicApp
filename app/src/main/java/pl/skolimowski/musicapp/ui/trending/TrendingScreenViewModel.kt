package pl.skolimowski.musicapp.ui.trending

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.TrendingTrack
import pl.skolimowski.musicapp.data.repository.Result
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.network.watcher.NetworkState
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcher
import javax.inject.Inject

sealed class TrendingScreenStates {
    data object Uninitialized : TrendingScreenStates()
    data object Loading : TrendingScreenStates()
    data object Success : TrendingScreenStates()
    data class Error(val errorType: ErrorType) : TrendingScreenStates()
}

data class TrendingScreenState(
    val screenState: TrendingScreenStates = TrendingScreenStates.Uninitialized,
    val trendingTracks: List<TrendingTrack> = emptyList(),
    val trackCacheStatus: Map<Uri, CacheStatus> = emptyMap()
)

enum class ErrorType {
    NO_NETWORK,
    SERVER_ERROR
}

sealed class TrendingScreenIntent {
    data object RefreshData : TrendingScreenIntent()
    data class TrackClicked(val trendingTrack: TrendingTrack) : TrendingScreenIntent()
    data class VisibleTracksChanged(val visibleTrackIds: Set<String>) : TrendingScreenIntent()
}

sealed class TrendingScreenSideEffect {
    data class NavigateToDetails(val trendingTrack: TrendingTrack) : TrendingScreenSideEffect()
}

interface ITrendingScreenViewModel {
    val state: StateFlow<TrendingScreenState>
    val sideEffect: Flow<TrendingScreenSideEffect>
    fun sendIntent(intent: TrendingScreenIntent)
}

@HiltViewModel
class TrendingScreenViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val networkStateWatcher: NetworkStateWatcher,
) : ViewModel(), ITrendingScreenViewModel {

    private val _state = MutableStateFlow<TrendingScreenState>(TrendingScreenState())
    override val state: StateFlow<TrendingScreenState> = _state.asStateFlow()

    private val _sideEffect = Channel<TrendingScreenSideEffect>(Channel.BUFFERED)
    override val sideEffect: Flow<TrendingScreenSideEffect> = _sideEffect.receiveAsFlow()

    private var prefetchJob: Job? = null

    init {
        observeNetworkStateAndLoad()
        observeTrendingTracks()
        observeCacheStatus()
    }

    private fun observeTrendingTracks() {
        viewModelScope.launch(Dispatchers.IO) {
            trackRepository.getTrendingTracks()
                .collectLatest { tracks ->
                    _state.update { it.copy(trendingTracks = tracks.map { it }) }
                }
        }
    }

    private fun observeNetworkStateAndLoad() {
        viewModelScope.launch(Dispatchers.Default) {
            networkStateWatcher.networkState
                .onEach { networkState ->
                    if (networkState == NetworkState.UNAVAILABLE && _state.value.screenState !is TrendingScreenStates.Success) {
                        _state.update { it.copy(screenState = TrendingScreenStates.Error(ErrorType.NO_NETWORK)) }
                    }
                }
                .distinctUntilChanged()
                .collect { networkState ->
                    if (networkState == NetworkState.AVAILABLE) {
                        if (_state.value.screenState !is TrendingScreenStates.Success && _state.value.screenState !is TrendingScreenStates.Loading) {
                            fetchTrendingTracks()
                        }
                    } else {
                        if (_state.value.screenState == TrendingScreenStates.Loading) {
                            _state.update {
                                it.copy(screenState = TrendingScreenStates.Error(ErrorType.NO_NETWORK))
                            }
                        }
                    }
                }
        }
    }

    private fun observeCacheStatus() {
        viewModelScope.launch(Dispatchers.Default) {
            trackRepository.getTrackCacheStatusFlow()
                .collectLatest { statusMap ->
                    _state.update { it.copy(trackCacheStatus = statusMap) }
                }
        }
    }

    override fun sendIntent(intent: TrendingScreenIntent) {
        Log.i("TrendingScreenViewModel", "sendIntent: $intent")

        when (intent) {
            is TrendingScreenIntent.RefreshData -> fetchTrendingTracks()
            is TrendingScreenIntent.TrackClicked -> {
                viewModelScope.launch(Dispatchers.Default) {
                    _sideEffect.send(TrendingScreenSideEffect.NavigateToDetails(intent.trendingTrack))
                }
            }

            is TrendingScreenIntent.VisibleTracksChanged -> {
                handleVisibleTracksChanged(intent.visibleTrackIds)
            }
        }
    }

    private fun handleVisibleTracksChanged(visibleTrackIds: Set<String>) {
        prefetchJob?.cancel()

        prefetchJob = viewModelScope.launch(Dispatchers.IO) {
            trackRepository.prefetchTrackStart(visibleTrackIds)
        }
    }

    private fun fetchTrendingTracks() {
        if (_state.value.screenState == TrendingScreenStates.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(screenState = TrendingScreenStates.Loading) }
            delay(10L) // this delay is required so that pull to refresh can be hidden

            if (networkStateWatcher.networkState.value == NetworkState.UNAVAILABLE) {
                _state.update { it.copy(screenState = TrendingScreenStates.Error(ErrorType.NO_NETWORK)) }

                return@launch
            }

            when (trackRepository.fetchTrendingTracks()) {
                is Result.Success -> {
                    _state.update { it.copy(screenState = TrendingScreenStates.Success) }
                }

                is Result.Error -> {
                    _state.update { it.copy(screenState = TrendingScreenStates.Error(ErrorType.SERVER_ERROR)) }
                }
            }
        }
    }
}