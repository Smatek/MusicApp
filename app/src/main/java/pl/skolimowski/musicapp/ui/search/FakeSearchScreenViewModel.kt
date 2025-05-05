package pl.skolimowski.musicapp.ui.search

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

// Fake implementation for Composable previews
class FakeSearchScreenViewModel(
    initialState: SearchScreenState = SearchScreenState()
) : ISearchScreenViewModel {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<SearchScreenState> = _state

    private val _sideEffect = MutableSharedFlow<SearchScreenSideEffect>()
    override val sideEffect: SharedFlow<SearchScreenSideEffect> = _sideEffect

    // Store last received intent for verification if needed
    var lastIntent: SearchScreenIntent? = null
        private set

    override fun sendIntent(intent: SearchScreenIntent) {
        lastIntent = intent
        // Simulate state changes based on intent for preview/testing
        when (intent) {
            is SearchScreenIntent.SearchQueryChanged -> {
                _state.value = _state.value.copy(query = intent.query)
                // Add mock results for preview if query is specific
                if (intent.query == "preview") {
                    // TODO: Add mock search results if needed for preview
                    // _state.value = _state.value.copy(searchResults = ..., screenState = SearchScreenStateEnum.SearchSuccess)
                } else if (intent.query.isBlank()) {
                    _state.value = _state.value.copy(screenState = SearchScreenStateEnum.Idle)
                }
            }
            // Add other intent handling if needed for previews
            else -> {}
        }
    }

    // Helper to manually set state for previews
    fun setState(newState: SearchScreenState) {
        _state.value = newState
    }
} 