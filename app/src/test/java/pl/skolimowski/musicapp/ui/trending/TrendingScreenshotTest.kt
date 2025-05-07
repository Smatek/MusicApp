package pl.skolimowski.musicapp.ui.trending

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import pl.skolimowski.musicapp.ui.PaparazziExt.gif

class TrendingScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun trendingScreenLoadingScreenshot() {
        paparazzi.gif("trending_screen_loading", end = 5000L) {
            TrendingScreenLoadingPreview()
        }
    }

    @Test
    fun trendingScreenWithContentScreenshot() {
        paparazzi.snapshot {
            TrendingScreenWithContentPreview()
        }
    }

    @Test
    fun trendingScreenEmptyScreenshot() {
        paparazzi.snapshot {
            TrendingScreenEmptyPreview()
        }
    }

    @Test
    fun trendingScreenNoNetworkErrorScreenshot() {
        paparazzi.snapshot {
            TrendingScreenNoNetworkErrorPreview()
        }
    }

    @Test
    fun trendingScreenServerErrorScreenshot() {
        paparazzi.snapshot {
            TrendingScreenServerErrorPreview()
        }
    }
} 