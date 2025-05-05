package pl.skolimowski.musicapp.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player

data class MusicPlaybackState(
    val isPlaying: Boolean = false,
    val currentMediaItem: MediaItem? = null,
    val playbackState: Int = Player.STATE_IDLE,
    val currentPositionMs: Long = 0,
    val durationMs: Long = 0,
    val hasNextMediaItem: Boolean = false,
    val hasPreviousMediaItem: Boolean = false,
    val error: String? = null // Optional: Add error state
) 