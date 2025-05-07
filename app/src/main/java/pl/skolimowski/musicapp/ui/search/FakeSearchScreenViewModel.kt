package pl.skolimowski.musicapp.ui.search

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSearchScreenViewModel(
    initialState: SearchScreenState = SearchScreenState()
) : ISearchScreenViewModel {

    override val state: StateFlow<SearchScreenState> = MutableStateFlow(initialState)

    override val sideEffect: SharedFlow<SearchScreenSideEffect> = MutableSharedFlow<SearchScreenSideEffect>()

    override fun sendIntent(intent: SearchScreenIntent) {
        // empty
    }
}