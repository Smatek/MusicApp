package pl.skolimowski.musicapp.ui.player

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme

@OptIn(ExperimentalSharedTransitionApi::class)
class PlayerScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun collapsedPlayerPlayingScreenshot() {
        paparazzi.snapshot {
            PlayerCollapsedPlayingPreview()
        }
    }

    @Test
    fun collapsedPlayerPausedScreenshot() {
        paparazzi.snapshot {
            PlayerCollapsedPausedPreview()
        }
    }

    @Test
    fun expandedPlayerPlayingScreenshot() {
        paparazzi.snapshot {
            PlayerExpandedPlayingPreview()
        }
    }

    @Test
    fun expandedPlayerPausedScreenshot() {
        paparazzi.snapshot {
            PlayerExpandedPausedPreview()
        }
    }

    @Test
    fun playerProgressAnimation() {
        val composeView = ComposeView(paparazzi.context)
        val trackDuration = 100_000L // 100 seconds

        composeView.setContent {
            var progress by remember { mutableFloatStateOf(0f) }

            LaunchedEffect(Unit) {
                while (progress < 1f) {
                    progress += 0.01f
                    kotlinx.coroutines.delay(100L)
                }
            }

            val fakeViewModel = FakePlayerViewModel(
                initialState = PlayerState(
                    currentTrackInfo = TrackInfoEntity(
                        "3", "Another Song", "Different Artist", trackDuration, artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    ),
                    isPlaying = true,
                    progress = progress,
                    isExpanded = true
                )
            )
            MusicAppTheme {
                SharedTransitionLayout {
                    Player(viewModel = fakeViewModel)
                }
            }
        }

        paparazzi.gif(
            composeView,
            name = "player_progress_animation",
            end = 2000L
        )
    }
}