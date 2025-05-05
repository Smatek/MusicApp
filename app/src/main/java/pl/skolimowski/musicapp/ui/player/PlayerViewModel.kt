package pl.skolimowski.musicapp.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.toTrackInfo
import pl.skolimowski.musicapp.data.repository.Result
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.di.DispatcherProvider
import pl.skolimowski.musicapp.player.MusicController
import javax.inject.Inject

data class PlayerState(
    val currentTrackInfo: TrackInfoEntity? = null,
    val isPlaying: Boolean = false,
    val progress: Float = 0f, // Progress 0.0 to 1.0
    val isExpanded: Boolean = false, // Added field for expansion state
    val hasNextTrack: Boolean = false,
    val hasPreviousTrack: Boolean = false
)

sealed class PlayerIntent {
    data class TrackSet(val track: Track) : PlayerIntent()
    data object PlayPauseClicked : PlayerIntent()
    data object PreviousClicked : PlayerIntent()
    data object NextClicked : PlayerIntent()
    data object RewindClicked : PlayerIntent()
    data object ForwardClicked : PlayerIntent()
    data class ToggleExpanded(val isExpanded: Boolean) : PlayerIntent()
    data class SeekPosition(val positionFraction: Float) :
        PlayerIntent() // Added intent for seeking

    data object SeekPositionFinally : PlayerIntent() // Added intent for seeking
}

interface IPlayerViewModel {
    val state: StateFlow<PlayerState>
    fun sendIntent(intent: PlayerIntent)
}

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val dispatchers: DispatcherProvider,
    private val musicController: MusicController,
    private val trackRepository: TrackRepository
) : ViewModel(), IPlayerViewModel {

    private val _state = MutableStateFlow(PlayerState())
    override val state: StateFlow<PlayerState> = _state.asStateFlow()

    var observeMusicStateJob: Job? = null

    init {
        observeMusicControllerState()
    }

    override fun sendIntent(intent: PlayerIntent) {
        when (intent) {
            is PlayerIntent.PlayPauseClicked -> handlePlayPause()
            is PlayerIntent.ToggleExpanded -> handleToggleExpanded(intent.isExpanded)
            is PlayerIntent.SeekPosition -> handleSeekPosition(intent.positionFraction)
            is PlayerIntent.SeekPositionFinally -> handleSeekPositionFinal()
            is PlayerIntent.TrackSet -> handleTrackSet(intent.track)
            is PlayerIntent.RewindClicked -> handleRewind()
            is PlayerIntent.ForwardClicked -> handleForward()
            is PlayerIntent.PreviousClicked -> handlePrevious()
            is PlayerIntent.NextClicked -> handleNext()
        }
    }

    private fun observeMusicControllerState() {
        observeMusicStateJob?.cancel()
        observeMusicStateJob = viewModelScope.launch(dispatchers.default) {
            musicController.playbackState.collectLatest { playbackState ->
                val currentMediaItem = playbackState.currentMediaItem
                val trackInfo = currentMediaItem?.toTrackInfo(playbackState.durationMs)

                val progress = if (playbackState.durationMs > 0) {
                    (playbackState.currentPositionMs.toFloat() / playbackState.durationMs)
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }

                _state.update {
                    it.copy(
                        currentTrackInfo = trackInfo,
                        isPlaying = playbackState.isPlaying,
                        progress = progress,
                        hasNextTrack = playbackState.hasNextMediaItem,
                        hasPreviousTrack = playbackState.hasPreviousMediaItem
                    )
                }
            }
        }
    }

    private fun stopObservingMusicState() {
        observeMusicStateJob?.cancel()
        observeMusicStateJob = null
    }

    private fun handleTrackSet(track: Track) {
        viewModelScope.launch(dispatchers.main) {
            musicController.pause()

            withContext(dispatchers.default) {
                // wait for music to be actually paused
                delay(100L)

                _state.update {
                    it.copy(
                        progress = 0f,
                        currentTrackInfo = track.trackInfo,
                        isExpanded = true,
                    )
                }
            }
        }

        viewModelScope.launch(dispatchers.default) {
            val trackWithStreamUrl = trackRepository.getTrack(track.trackInfo.id)

            if (trackWithStreamUrl is Result.Success) {
                withContext(dispatchers.main) {
                    musicController.setTrack(trackWithStreamUrl.data)
                    musicController.play()
                }
            } else {
                Log.e("PlayerViewModel", "Failed to fetch stream URL")
            }
        }
    }

    private fun handlePlayPause() {
        if (musicController.playbackState.value.isPlaying) {
            musicController.pause()
        } else {
            musicController.play()
        }
    }

    private fun handleToggleExpanded(expanded: Boolean) {
        _state.update { it.copy(isExpanded = expanded) }
    }

    private fun handleSeekPosition(positionFraction: Float) {
        // stop observing to avoid triggering playback state updates during progress change
        stopObservingMusicState()

        _state.update { it.copy(progress = positionFraction) }
    }

    private fun handleSeekPositionFinal() {
        val duration = musicController.playbackState.value.durationMs

        if (duration > 0) {
            val seekPositionMs = (duration * state.value.progress).toLong()
            musicController.seekTo(seekPositionMs)
        }

        observeMusicControllerState()
    }

    private fun handleRewind() {
        val currentPosition = musicController.playbackState.value.currentPositionMs
        val newPosition = (currentPosition - 10_000L).coerceAtLeast(0L)
        musicController.seekTo(newPosition)
    }

    private fun handleForward() {
        val currentPosition = musicController.playbackState.value.currentPositionMs
        val duration = musicController.playbackState.value.durationMs
        if (duration > 0) {
            val newPosition = (currentPosition + 10_000L).coerceAtMost(duration)
            musicController.seekTo(newPosition)
        } else {
            // Cannot forward if duration is unknown or zero
            Log.w("PlayerViewModel", "Cannot forward: duration unknown or zero")
        }
    }

    private fun handlePrevious() {
        val canSkipPrevious = musicController.playbackState.value.hasPreviousMediaItem
        val isPastResetThreshold = musicController.playbackState.value.currentPositionMs < 2000L

        if (canSkipPrevious && isPastResetThreshold) {
            musicController.skipPrevious()
        } else {
            musicController.seekTo(0L)
        }
    }

    private fun handleNext() {
        musicController.skipNext()
    }
}