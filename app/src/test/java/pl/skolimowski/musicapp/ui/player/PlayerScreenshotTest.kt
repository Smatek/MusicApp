package pl.skolimowski.musicapp.ui.player

import androidx.compose.animation.ExperimentalSharedTransitionApi
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

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
}