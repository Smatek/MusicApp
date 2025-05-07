package pl.skolimowski.musicapp.ui.playlistdetails

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class PlaylistDetailsScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun playlistDetailsLoadingScreenshot() {
        paparazzi.snapshot {
            PlaylistDetailsLoadingPreview()
        }
    }

    @Test
    fun playlistDetailsWithContentScreenshot() {
        paparazzi.snapshot {
            PlaylistDetailsWithContentPreview()
        }
    }

    @Test
    fun playlistDetailsErrorScreenshot() {
        paparazzi.snapshot {
            PlaylistDetailsErrorPreview()
        }
    }
} 