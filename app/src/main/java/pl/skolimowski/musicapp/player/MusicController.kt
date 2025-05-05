package pl.skolimowski.musicapp.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.di.DispatcherProvider
import javax.inject.Inject
import javax.inject.Singleton

interface MusicController {
    val playbackState: StateFlow<MusicPlaybackState>
    suspend fun connect()
    fun release()
    fun play()
    fun pause()
    fun seekTo(positionMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun setTrack(track: Track)
    fun addTrack(track: Track)
}

@Singleton
class MusicControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : MusicController, Player.Listener {

    private val _playbackState = MutableStateFlow(MusicPlaybackState())
    override val playbackState: StateFlow<MusicPlaybackState> = _playbackState.asStateFlow()

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture?.isDone == true) mediaControllerFuture?.get() else null

    // SupervisorJob allows child coroutines to fail independently
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var connectionJob: Job? = null
    private var positionUpdateJob: Job? = null

    companion object {
        private const val TAG = "MusicControllerImpl"
        private const val POSITION_UPDATE_INTERVAL_MS = 500L // Update position 10 times/sec
    }

    override suspend fun connect() {
        Log.d(TAG, "Attempting to connect...")
        if (mediaController != null || connectionJob?.isActive == true) {
            Log.d(TAG, "Already connected or connecting.")
            return
        }
        connectionJob = scope.launch(dispatchers.io) {
            try {
                val sessionToken =
                    SessionToken(context, ComponentName(context, PlaybackService::class.java))
                mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
                // We don't await here to avoid blocking the caller.
                // The listener will handle the connection result.
                mediaControllerFuture?.addListener({
                    try {
                        val controller = mediaControllerFuture?.get()

                        if (controller != null) {
                            controller.addListener(this@MusicControllerImpl)
                            Log.d(TAG, "MediaController connected successfully.")
                            updateState()
                        } else {
                            Log.e(TAG, "MediaController future returned null controller.")
                            _playbackState.update { it.copy(error = "Failed to get MediaController instance.") }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error getting MediaController from future: ${e.message}", e)
                        _playbackState.update { it.copy(error = "Failed to connect to PlaybackService: ${e.message}") }
                    }
                }, ContextCompat.getMainExecutor(context))

            } catch (e: Exception) {
                Log.e(TAG, "Error building MediaController future: ${e.message}", e)
                _playbackState.update { it.copy(error = "Failed to initiate connection: ${e.message}") }
            }
        }
    }

    override fun release() {
        Log.d(TAG, "Releasing MusicController...")
        connectionJob?.cancel()
        positionUpdateJob?.cancel()
        connectionJob = null
        positionUpdateJob = null
        mediaController?.removeListener(this)
        mediaController?.stop()
        mediaController?.release()
        // Release the controller via its future.
        mediaControllerFuture?.let { future ->
            MediaController.releaseFuture(future)
            Log.d(TAG, "MediaController future released.")
        }
        mediaControllerFuture = null
        _playbackState.value = MusicPlaybackState() // Reset state
        scope.launch { // Clear potential errors after release
            _playbackState.update { it.copy(error = null) }
        }
        Log.d(TAG, "MusicController released completely.")
    }

    // --- Player Controls ---
    override fun play() {
        mediaController?.play()
    }

    override fun pause() {
        mediaController?.pause()
    }

    override fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    override fun skipNext() {
        mediaController?.seekToNextMediaItem()
    }

    override fun skipPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    override fun setTrack(track: Track) {
        val mediaItem = mapTrackToMediaItem(track)

        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
    }

    override fun addTrack(track: Track) {
        val mediaItem = mapTrackToMediaItem(track)

        mediaController?.addMediaItem(mediaItem)
    }

    private fun mapTrackToMediaItem(track: Track): MediaItem {
        val trackInfo = track.trackInfo

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(trackInfo.title)
            .setArtist(trackInfo.artist)
            .setArtworkUri(trackInfo.artwork?.url480x480?.toUri())
            .build()

        return MediaItem.Builder()
            .setMediaId(trackInfo.id)
            .setUri(track.streamUrl!!.url.toUri())
            .setMediaMetadata(mediaMetadata)
            .build()
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Log.d(TAG, "onIsPlayingChanged: $isPlaying")

        updateState()
        if (isPlaying) {
            startPositionUpdates()
        } else {
            stopPositionUpdates()
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        if (events.containsAny(
                Player.EVENT_PLAYBACK_STATE_CHANGED,
                Player.EVENT_MEDIA_ITEM_TRANSITION,
                Player.EVENT_POSITION_DISCONTINUITY, // Handle seeks or skips
                Player.EVENT_TIMELINE_CHANGED, // Duration might change
                Player.EVENT_IS_PLAYING_CHANGED
            )
        ) {
            updateState()
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        Log.d(TAG, "onMediaItemTransition: New item: ${mediaItem?.mediaId}, Reason: $reason")
        updateState()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        Log.d(TAG, "onPlaybackStateChanged: ${playerStateToString(playbackState)}")
        updateState()

        when (playbackState) {
            Player.STATE_ENDED -> stopPositionUpdates()
            Player.STATE_READY -> {
                if (mediaController?.isPlaying == true) startPositionUpdates()
            }

            Player.STATE_BUFFERING, Player.STATE_IDLE -> stopPositionUpdates()
            else -> {}
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        Log.e(TAG, "Player Error: ${error.message} Code: ${error.errorCodeName}", error)
        _playbackState.update { it.copy(error = "Player error: ${error.message ?: error.errorCodeName}") }
        stopPositionUpdates()
    }

    override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
        Log.d(TAG, "onTimelineChanged, Reason: $reason")
        updateState()
    }

    private fun updateState() {
        val controller = mediaController ?: run {
            Log.d(TAG, "updateState called but MediaController is null.")
            return
        }
        scope.launch(Dispatchers.Main.immediate) { // Ensure state updates happen ASAP on Main
            val currentState = _playbackState.value
            val newState = currentState.copy(
                isPlaying = controller.isPlaying,
                currentMediaItem = controller.currentMediaItem,
                playbackState = controller.playbackState,
                // Use currentPosition from controller only if not actively updating via job
                currentPositionMs = if (positionUpdateJob?.isActive == true) currentState.currentPositionMs else controller.currentPosition,
                durationMs = controller.duration.coerceAtLeast(0), // Ensure duration is not negative
                hasNextMediaItem = controller.hasNextMediaItem(),
                hasPreviousMediaItem = controller.hasPreviousMediaItem()
                // Keep existing error unless explicitly cleared elsewhere
                // error = null // Only clear error on successful playback events? Needs careful handling.
            )
            // Only update if something actually changed to avoid redundant emissions
            if (newState != currentState) {
                _playbackState.value = newState
                Log.d(
                    TAG,
                    "State updated: Play=${newState.isPlaying}, State=${playerStateToString(newState.playbackState)}, Item=${newState.currentMediaItem?.mediaId}, Pos=${newState.currentPositionMs}, Dur=${newState.durationMs}"
                )
            }
        }
    }

    private fun startPositionUpdates() {
        if (positionUpdateJob?.isActive == true) return // Already running
        Log.d(TAG, "Starting position updates.")
        positionUpdateJob = scope.launch {
            while (isActive && mediaController?.isPlaying == true) {
                val currentPosition = mediaController?.currentPosition ?: 0
                if (_playbackState.value.currentPositionMs != currentPosition) {
                    _playbackState.update { it.copy(currentPositionMs = currentPosition) }
                }
                delay(POSITION_UPDATE_INTERVAL_MS)
            }
            Log.d(TAG, "Position updates stopped.")
        }
    }

    private fun stopPositionUpdates() {
        if (positionUpdateJob?.isActive == true) {
            Log.d(TAG, "Stopping position updates.")
            positionUpdateJob?.cancel()
            positionUpdateJob = null
            // Ensure the final position is updated accurately
            updateState()
        }
    }

    private fun playerStateToString(state: Int): String {
        return when (state) {
            Player.STATE_IDLE -> "IDLE"
            Player.STATE_BUFFERING -> "BUFFERING"
            Player.STATE_READY -> "READY"
            Player.STATE_ENDED -> "ENDED"
            else -> "UNKNOWN"
        }
    }
} 