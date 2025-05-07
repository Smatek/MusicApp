package pl.skolimowski.musicapp.ui.playlist

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.skolimowski.musicapp.ui.PaparazziExt.gif

class PlaylistScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun playlistScreenLoadingScreenshot() {
        paparazzi.gif(name = "playlist_loading", end = 5000L) {
            PlaylistScreenLoadingPreview()
        }
    }

    @Test
    fun playlistScreenWithContentScreenshot() {
        paparazzi.snapshot {
            PlaylistScreenWithContentPreview()
        }
    }

    @Test
    fun playlistScreenErrorScreenshot() {
        paparazzi.snapshot {
            PlaylistScreenErrorPreview()
        }
    }
} 