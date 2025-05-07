package pl.skolimowski.musicapp.ui.search

import androidx.compose.runtime.Composable
import pl.skolimowski.musicapp.data.model.RecentlySearchedTrackImpl
import pl.skolimowski.musicapp.data.model.TrackImpl
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.common.MaterialPreview
import pl.skolimowski.musicapp.ui.common.PreviewContainer

@MaterialPreview
@Composable
fun SearchScreenIdleEmptyPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            screenState = SearchScreenStateEnum.Idle
        )
    )
    PreviewContainer {
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun SearchScreenIdleWithRecentPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            screenState = SearchScreenStateEnum.Idle,
            recentSearches = listOf(
                RecentlySearchedTrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "1",
                        title = "Recently Played Song",
                        artist = "Artist 1",
                        duration = 180000L,
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
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun SearchScreenSearchingPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            query = "test",
            isLoading = true,
            screenState = SearchScreenStateEnum.Search
        )
    )
    PreviewContainer {
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun SearchScreenWithResultsPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            query = "test",
            screenState = SearchScreenStateEnum.Search,
            searchResults = listOf(
                TrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "1",
                        title = "Test Song 1",
                        artist = "Artist 1",
                        duration = 180000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    ),
                    streamUrl = null
                ),
                TrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "2",
                        title = "Test Song 2",
                        artist = "Artist 2",
                        duration = 240000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    ),
                    streamUrl = null
                )
            )
        )
    )
    PreviewContainer {
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun SearchScreenNoResultsPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            query = "nonexistent",
            screenState = SearchScreenStateEnum.Search
        )
    )
    PreviewContainer {
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
}

@MaterialPreview
@Composable
fun SearchScreenErrorPreview() {
    val fakeViewModel = FakeSearchScreenViewModel(
        initialState = SearchScreenState(
            screenState = SearchScreenStateEnum.Error(SearchErrorType.NO_NETWORK)
        )
    )
    PreviewContainer {
        SearchScreen(
            viewModel = fakeViewModel,
            onTrackClicked = {}
        )
    }
} 