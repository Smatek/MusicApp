@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.skolimowski.musicapp.ui.player

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme

@Preview(name = "Collapsed Player Playing")
@Composable
private fun PlayerCollapsedPlayingPreview() {
    val fakeViewModel = FakePlayerViewModel(
        initialState = PlayerState(
            currentTrackInfo = TrackInfoEntity(
                id = "1",
                title = "A Very Long Track Title That Might Overflow",
                artist = "An Equally Long Artist Name",
                duration = 180000L,
                artwork = Artwork(
                    url150x150 = "",
                    url480x480 = ""
                )
            ),
            isPlaying = true,
            progress = 0.4f,
            isExpanded = false
        )
    )
    MusicAppTheme {
        // Wrap preview in SharedTransitionLayout for it to compile
        SharedTransitionLayout {
            Player(viewModel = fakeViewModel)
        }
    }
}

@Preview(name = "Collapsed Player Paused")
@Composable
private fun PlayerCollapsedPausedPreview() {
    val fakeViewModel = FakePlayerViewModel(
        initialState = PlayerState(
            currentTrackInfo = TrackInfoEntity(
                "2", "Short Song", "Artist", 90000L, artwork = Artwork(
                    url150x150 = "",
                    url480x480 = ""
                )
            ),
            isPlaying = false,
            progress = 0.1f,
            isExpanded = false
        )
    )
    MusicAppTheme {
        SharedTransitionLayout {
            Player(viewModel = fakeViewModel)
        }
    }
}

@Preview(name = "Expanded Player Playing", showBackground = true, heightDp = 800)
@Composable
private fun PlayerExpandedPlayingPreview() {
    val fakeViewModel = FakePlayerViewModel(
        initialState = PlayerState(
            currentTrackInfo = TrackInfoEntity(
                id = "1",
                title = "A Very Long Track Title That Might Overflow Into Two Lines Maybe",
                artist = "An Equally Long Artist Name",
                duration = 245000L, // 4:05
                artwork = Artwork(
                    url150x150 = "",
                    url480x480 = ""
                )
            ),
            isPlaying = true,
            progress = 0.6f, // Example progress
            isExpanded = true
        )
    )
    MusicAppTheme {
        SharedTransitionLayout {
            Player(viewModel = fakeViewModel)
        }
    }
}


@Preview(name = "Expanded Player Paused", showBackground = true, heightDp = 800)
@Composable
private fun PlayerExpandedPausedPreview() {
    val fakeViewModel = FakePlayerViewModel(
        initialState = PlayerState(
            currentTrackInfo = TrackInfoEntity(
                "3", "Another Song", "Different Artist", 150000L, artwork = Artwork(
                    url150x150 = "",
                    url480x480 = ""
                )
            ),
            isPlaying = false,
            progress = 0.2f,
            isExpanded = true
        )
    )
    MusicAppTheme {
        SharedTransitionLayout {
            Player(viewModel = fakeViewModel)
        }
    }
}

@Preview(name = "Collapsed - No Track")
@Composable
private fun PlayerNoTrackPreview() {
    val fakeViewModel = FakePlayerViewModel(
        initialState = PlayerState(currentTrackInfo = null)
    )
    MusicAppTheme {
        SharedTransitionLayout {
            Player(viewModel = fakeViewModel) // Should show nothing
        }
    }
}