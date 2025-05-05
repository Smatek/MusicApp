package pl.skolimowski.musicapp.ui.search

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.cache.ITrackCacheManager
import pl.skolimowski.musicapp.data.model.RecentlySearchedTrack
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.data.repository.Result
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.di.DispatcherProvider
import pl.skolimowski.musicapp.network.watcher.NetworkState
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcher
import pl.skolimowski.musicapp.player.MusicController
import javax.inject.Inject

// Basic state holder for Search screen (Moved here)
data class SearchScreenState(
    val isLoading: Boolean = false,
    val query: String = "",
    val screenState: SearchScreenStateEnum = SearchScreenStateEnum.Idle,
    val searchResults: List<Track> = emptyList(),
    val recentSearches: List<RecentlySearchedTrack> = emptyList(),
    val trackCacheStatus: Map<Uri, CacheStatus> = emptyMap()
)

sealed class SearchScreenStateEnum {
    data object Idle : SearchScreenStateEnum()
    data object Search : SearchScreenStateEnum()
    data class Error(val errorType: SearchErrorType) : SearchScreenStateEnum()
}

enum class SearchErrorType {
    NO_NETWORK,
    SERVER_ERROR
}

// Interface for SearchScreenViewModel for better testability and previews
interface ISearchScreenViewModel {
    val state: StateFlow<SearchScreenState>
    val sideEffect: Flow<SearchScreenSideEffect>
    fun sendIntent(intent: SearchScreenIntent)
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val networkStateWatcher: NetworkStateWatcher,
    private val musicController: MusicController,
    private val trackCacheManager: ITrackCacheManager,
    private val dispatchers: DispatcherProvider
) : ViewModel(), ISearchScreenViewModel {

    private val _state = MutableStateFlow(SearchScreenState())
    override val state: StateFlow<SearchScreenState> = _state.asStateFlow()

    private val _sideEffect = Channel<SearchScreenSideEffect>(Channel.BUFFERED)
    override val sideEffect: Flow<SearchScreenSideEffect> = _sideEffect.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")

    private var observeTrackMatchingQueryJob: Job? = null
    private var searchTrackJob: Job? = null
    private var prefetchJob: Job? = null

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 500L
        private const val TAG = "SearchScreenVM"
    }

    init {
        observeNetworkState()
        observeRecentSearches()
        observeSearchQuery()
        observeCacheStatus()
    }

    private fun observeNetworkState() {
        networkStateWatcher.networkState
            .onEach { networkState ->
                val currentState = _state.value.screenState
                if (networkState == NetworkState.UNAVAILABLE && currentState is SearchScreenStateEnum.Search) {
                    Log.w(TAG, "Network unavailable, setting error state.")
                    _state.update {
                        it.copy(
                            screenState = SearchScreenStateEnum.Error(
                                SearchErrorType.NO_NETWORK
                            )
                        )
                    }
                }
                // Optionally re-trigger search if network comes back and we were in error state?
                // else if (networkState == NetworkState.AVAILABLE && currentState is SearchScreenStateEnum.Error && currentState.errorType == SearchErrorType.NO_NETWORK) {
                //    performSearch(searchQuery.value)
                // }
            }
            .flowOn(dispatchers.io)
            .launchIn(viewModelScope)
    }

    private fun observeRecentSearches() {
        trackRepository.getRecentlySearchedTracks()
            .onEach { recentTracks ->
                // Update recent searches list regardless of current state
                _state.update { it.copy(recentSearches = recentTracks) }
                // Only potentially change screen state if the query is empty
                if (state.value.query.isEmpty()) {
                    _state.update { it.copy(screenState = SearchScreenStateEnum.Idle) }
                }
            }
            .catch { e -> Log.e(TAG, "Error observing recent searches", e) } // Add error handling
            .flowOn(dispatchers.io)
            .launchIn(viewModelScope)
    }

    private fun observeSearchQuery() {
        searchQuery
            .debounce { query ->
                if(query.isEmpty()) { 0L } else { SEARCH_DEBOUNCE_MS }
            }
            .distinctUntilChanged()
            .onEach { query ->
                _state.update { it.copy(query = query) } // Update query state immediately
                if (query.isBlank()) {
                    Log.d(TAG, "Query blank, resetting to Idle/Recent state.")
                    // Show recent searches when query is blank
                    _state.update {
                        it.copy(
                            screenState = SearchScreenStateEnum.Idle,
                            searchResults = emptyList()
                        )
                    }
                } else {
                    performSearch(query)
                }
            }
            .flowOn(dispatchers.default)
            .launchIn(viewModelScope)
    }

    private fun observeCacheStatus() {
        trackCacheManager.cacheStatusFlow
            .onEach { cacheMap ->
                _state.update { it.copy(trackCacheStatus = cacheMap) }
            }
            .catch { e -> Log.e(TAG, "Error observing cache status", e) } // Add error handling
            .flowOn(dispatchers.io)
            .launchIn(viewModelScope)
    }

    private fun performSearch(query: String) {
        if (networkStateWatcher.networkState.value == NetworkState.UNAVAILABLE) {
            Log.w(TAG, "Search aborted: No network.")
            _state.update { it.copy(screenState = SearchScreenStateEnum.Error(SearchErrorType.NO_NETWORK)) }
            return
        }

        observeTrackMatchingQueryJob?.cancel()
        observeTrackMatchingQueryJob = viewModelScope.launch(dispatchers.io) {
            trackRepository.observeTrackMatchingQuery(query).collectLatest { searchResults ->
                _state.update { it.copy(searchResults = searchResults) }
            }
        }

        searchTrackJob?.cancel()
        searchTrackJob = viewModelScope.launch(dispatchers.io) {
            Log.d(TAG, "Performing search for: $query")
            _state.update { it.copy(isLoading = true, screenState = SearchScreenStateEnum.Search) }

            when (val result = trackRepository.searchTracks(query)) {
                is Result.Success -> {
                    Log.d(TAG, "Search successful for '$query', ${result.data.size} results.")
                }

                is Result.Error -> {
                    Log.e(TAG, "Search failed for query '$query'", result.exception)

                    if (result.exception !is CancellationException) {
                        _state.update {
                            it.copy(
                                screenState = SearchScreenStateEnum.Error(
                                    SearchErrorType.SERVER_ERROR
                                ), searchResults = emptyList()
                            )
                        }
                    }
                }
            }

            _state.update { it.copy(isLoading = false) }
        }
    }

    override fun sendIntent(intent: SearchScreenIntent) {
        // Process intents outside the viewModelScope launch unless they trigger a coroutine
        when (intent) {
            is SearchScreenIntent.SearchQueryChanged -> {
                // Update the StateFlow, the collector (observeSearchQuery) will handle the rest
                searchQuery.value = intent.query
            }

            is SearchScreenIntent.TrackClicked -> {
                handleTrackClicked(intent.track)
            }

            is SearchScreenIntent.VisibleTracksChanged -> {
                handleVisibleTracksChanged(intent.visibleTrackIds)
            }
        }
    }

    private fun handleTrackClicked(track: Track) {
        viewModelScope.launch(dispatchers.io) {
            Log.d(TAG, "Track clicked: ${track.trackInfo.id}, saving to recent.")
            trackRepository.saveRecentlySearchedTrack(track.trackInfo)

            viewModelScope.launch(dispatchers.default) {
                _sideEffect.send(SearchScreenSideEffect.NavigateToDetails(track))
            }
        }
    }

    private fun handleVisibleTracksChanged(visibleTrackIds: Set<String>) {
        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch(dispatchers.io) {
            trackRepository.prefetchTrackStart(visibleTrackIds)
        }
    }
} 