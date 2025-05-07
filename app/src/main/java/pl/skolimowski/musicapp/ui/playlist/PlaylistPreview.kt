package pl.skolimowski.musicapp.ui.playlist

import androidx.compose.runtime.Composable
import pl.skolimowski.musicapp.data.model.Playlist
import pl.skolimowski.musicapp.data.model.PlaylistImpl
import pl.skolimowski.musicapp.data.model.TrackImpl
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.common.MaterialPreview
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme

@MaterialPreview
@Composable
fun PlaylistScreenLoadingPreview() {
    val fakeViewModel = FakePlaylistViewModel(
        initialState = PlaylistState(
            isLoading = true
        )
    )
    MusicAppTheme {
        PlaylistScreen(
            viewModel = fakeViewModel,
            onPlaylistClick = {}
        )
    }
}

@MaterialPreview
@Composable
fun PlaylistScreenWithContentPreview() {
    val fakeViewModel = FakePlaylistViewModel(
        initialState = PlaylistState(
            trendingPlaylists = listOf(
                PlaylistImpl(
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
                        )
                    )
                ),
                PlaylistImpl(
                    playlistEntity = PlaylistEntity(
                        id = "2",
                        name = "Chill Vibes",
                        artworkUrl = null,
                        description = "Relaxing music"
                    ),
                    playlistTracks = emptyList()
                )
            )
        )
    )
    MusicAppTheme {
        PlaylistScreen(
            viewModel = fakeViewModel,
            onPlaylistClick = {}
        )
    }
}

@MaterialPreview
@Composable
fun PlaylistScreenErrorPreview() {
    val fakeViewModel = FakePlaylistViewModel(
        initialState = PlaylistState(
            error = "Failed to load playlists"
        )
    )
    MusicAppTheme {
        PlaylistScreen(
            viewModel = fakeViewModel,
            onPlaylistClick = {}
        )
    }
} 