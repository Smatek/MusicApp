package pl.skolimowski.musicapp.ui.trending

import androidx.compose.runtime.Composable
import pl.skolimowski.musicapp.data.model.TrendingTrackImpl
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.common.MaterialPreview
import pl.skolimowski.musicapp.ui.common.PreviewContainer

@MaterialPreview
@Composable
fun TrendingScreenLoadingPreview() {
    val fakeViewModel = FakeTrendingScreenViewModel(
        initialState = TrendingScreenState(
            screenState = TrendingScreenStates.Loading
        )
    )
    PreviewContainer {
        TrendingScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun TrendingScreenWithContentPreview() {
    val fakeViewModel = FakeTrendingScreenViewModel(
        initialState = TrendingScreenState(
            screenState = TrendingScreenStates.Success,
            trendingTracks = listOf(
                TrendingTrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "1",
                        title = "Trending Song 1",
                        artist = "Artist 1",
                        duration = 180000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    )
                ),
                TrendingTrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "2",
                        title = "Trending Song 2",
                        artist = "Artist 2",
                        duration = 240000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    )
                )
            )
        )
    )
    PreviewContainer {
        TrendingScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun TrendingScreenEmptyPreview() {
    val fakeViewModel = FakeTrendingScreenViewModel(
        initialState = TrendingScreenState(
            screenState = TrendingScreenStates.Success
        )
    )
    PreviewContainer {
        TrendingScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun TrendingScreenNoNetworkErrorPreview() {
    val fakeViewModel = FakeTrendingScreenViewModel(
        initialState = TrendingScreenState(
            screenState = TrendingScreenStates.Error(ErrorType.NO_NETWORK)
        )
    )
    PreviewContainer {
        TrendingScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun TrendingScreenServerErrorPreview() {
    val fakeViewModel = FakeTrendingScreenViewModel(
        initialState = TrendingScreenState(
            screenState = TrendingScreenStates.Error(ErrorType.SERVER_ERROR)
        )
    )
    PreviewContainer {
        TrendingScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
} 