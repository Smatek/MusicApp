package pl.skolimowski.musicapp.ui.search

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test

class SearchScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun searchScreenIdleEmptyScreenshot() {
        paparazzi.snapshot {
            SearchScreenIdleEmptyPreview()
        }
    }

    @Test
    fun searchScreenIdleWithRecentScreenshot() {
        paparazzi.snapshot {
            SearchScreenIdleWithRecentPreview()
        }
    }

    @Test
    fun searchScreenSearchingScreenshot() {
        paparazzi.snapshot {
            SearchScreenSearchingPreview()
        }
    }

    @Test
    fun searchScreenWithResultsScreenshot() {
        paparazzi.snapshot {
            SearchScreenWithResultsPreview()
        }
    }

    @Test
    fun searchScreenNoResultsScreenshot() {
        paparazzi.snapshot {
            SearchScreenNoResultsPreview()
        }
    }

    @Test
    fun searchScreenErrorScreenshot() {
        paparazzi.snapshot {
            SearchScreenErrorPreview()
        }
    }
} 