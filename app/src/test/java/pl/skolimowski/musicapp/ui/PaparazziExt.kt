package pl.skolimowski.musicapp.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import app.cash.paparazzi.Paparazzi

object PaparazziExt {
    fun Paparazzi.gif(
        name: String,
        start: Long = 0L,
        end: Long = 500L,
        composable: @Composable () -> Unit
    ) {
        val composeView = ComposeView(this.context)

        composeView.setContent {
            composable()
        }

        this.gif(
            view = composeView,
            name = name,
            start = start,
            end = end,
        )
    }
}