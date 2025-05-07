package pl.skolimowski.musicapp.ui.playlistdetails

import androidx.compose.runtime.Composable
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.PlaylistImpl
import pl.skolimowski.musicapp.data.model.TrackImpl
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.common.MaterialPreview
import pl.skolimowski.musicapp.ui.common.PreviewContainer

@MaterialPreview
@Composable
fun PlaylistDetailsLoadingPreview() {
    val fakeViewModel = FakePlaylistDetailsViewModel(
        initialState = PlaylistDetailsState(
            isLoading = true
        )
    )
    PreviewContainer {
        PlaylistDetailsScreen(viewModel = fakeViewModel)
    }
}

@MaterialPreview
@Composable
fun PlaylistDetailsWithContentPreview() {
    val fakeViewModel = FakePlaylistDetailsViewModel(
        initialState = PlaylistDetailsState(
            playlist = PlaylistImpl(
                playlistEntity = PlaylistEntity(
                    id = "1",
                    name = "Top Hits 2024",
                    artworkUrl = "https://example.com/artwork1.jpg",
                    description = "Best hits of 2024"
                ),
                playlistTracks = listOf(
                    TrackImpl(
                        trackInfo = TrackInfoEntity(
                            id = "1",
                            title = "Song 1",
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
                            title = "Song 2",
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
            ),
            tracksWithCacheStatus = mapOf(
                TrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "1",
                        title = "Song 1",
                        artist = "Artist 1",
                        duration = 180000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    ),
                    streamUrl = null
                ) to CacheStatus.CACHED,
                TrackImpl(
                    trackInfo = TrackInfoEntity(
                        id = "2",
                        title = "Song 2",
                        artist = "Artist 2",
                        duration = 240000L,
                        artwork = Artwork(
                            url150x150 = "",
                            url480x480 = ""
                        )
                    ),
                    streamUrl = null
                ) to CacheStatus.NOT_CACHED
            )
        )
    )
    PreviewContainer {
        PlaylistDetailsScreen(viewModel = fakeViewModel)
    }
}

@MaterialPreview
@Composable
fun PlaylistDetailsErrorPreview() {
    val fakeViewModel = FakePlaylistDetailsViewModel(
        initialState = PlaylistDetailsState(
            error = "Failed to load playlist details"
        )
    )
    PreviewContainer {
        PlaylistDetailsScreen(viewModel = fakeViewModel)
    }
} 