package pl.skolimowski.musicapp.ui.trending

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class FakeTrendingScreenViewModel(
    initialState: TrendingScreenState = TrendingScreenState(),
): ITrendingScreenViewModel {
    override val state: StateFlow<TrendingScreenState> = MutableStateFlow(initialState)
    override val sideEffect: Flow<TrendingScreenSideEffect> = Channel<TrendingScreenSideEffect>(Channel.BUFFERED).receiveAsFlow()

    override fun sendIntent(intent: TrendingScreenIntent) {
        // empty
    }
}