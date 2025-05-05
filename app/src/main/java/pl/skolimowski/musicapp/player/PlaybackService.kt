package pl.skolimowski.musicapp.player

import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC
import androidx.media3.common.C.USAGE_MEDIA
import androidx.media3.datasource.DataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import pl.skolimowski.musicapp.di.CachingDataSource
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    @Inject
    @CachingDataSource
    lateinit var dataSourceFactory: DataSource.Factory

    companion object {
        private const val TAG = "PlaybackService"
    }

    override fun onCreate() {
        super.onCreate()

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(USAGE_MEDIA)
            .build()

        try {
            val player = ExoPlayer.Builder(this)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .setMediaSourceFactory(DefaultMediaSourceFactory(this)
                    .setDataSourceFactory(dataSourceFactory))
                .build()

            mediaSession = MediaSession.Builder(this, player).build()
            Log.d(TAG, "onCreate: Player and MediaSession created successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error creating player or media session", e)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
         Log.d(TAG, "onGetSession: Connection request from ${controllerInfo.packageName}")
         return mediaSession
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: Service destroying...")

        mediaSession?.run {
            Log.d(TAG, "Releasing player and media session.")
            // Get the player instance from the session before releasing the session
            val playerToRelease = player
            release()
            playerToRelease.release() // Release the player instance
            mediaSession = null
             Log.d(TAG, "Player and media session released.")
        }
        super.onDestroy()
         Log.d(TAG, "onDestroy: Service destroyed.")
    }

     // Clean up resources when task is removed
    override fun onTaskRemoved(rootIntent: android.content.Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
             Log.d(TAG, "onTaskRemoved: Stopping service as player is not playing or has no items.")
            // Stop the service if not playing
            stopSelf()
        } else {
             Log.d(TAG, "onTaskRemoved: Player is active, service continues.")
        }
        super.onTaskRemoved(rootIntent)
    }
} 