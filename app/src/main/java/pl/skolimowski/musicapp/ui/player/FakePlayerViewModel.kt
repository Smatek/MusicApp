package pl.skolimowski.musicapp.ui.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakePlayerViewModel(
    initialState: PlayerState = PlayerState(),
    override val state: StateFlow<PlayerState> = MutableStateFlow(initialState)
): IPlayerViewModel {
    override fun sendIntent(intent: PlayerIntent) {
        if(intent is PlayerIntent.ToggleExpanded) {
            (state as MutableStateFlow).value = state.value.copy(
                isExpanded = !state.value.isExpanded
            )
        }
    }
}