package pl.skolimowski.musicapp.ui.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlayerViewModel(
    initialState: PlayerState = PlayerState(),
): IPlayerViewModel {
    override val state: StateFlow<PlayerState> = MutableStateFlow(initialState)

    override fun sendIntent(intent: PlayerIntent) {
        // empty
    }
}