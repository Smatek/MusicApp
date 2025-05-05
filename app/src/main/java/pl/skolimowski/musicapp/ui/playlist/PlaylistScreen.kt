package pl.skolimowski.musicapp.ui.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

sealed class PlaylistIntent {
    object Refresh : PlaylistIntent()
}

@Composable
fun PlaylistScreen(
    viewModel: PlaylistViewModel = hiltViewModel(),
    onPlaylistClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            state.error != null -> {
                Text(
                    text = state.error ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    if (state.isLoading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    } else {
                        val trendingPlaylists = state.trendingPlaylists
                        items(
                            trendingPlaylists,
                            key = { playlist -> playlist.playlistEntity.id }) { playlist ->
                            PlaylistItem(
                                modifier = Modifier.clickable { onPlaylistClick(playlist.playlistEntity.id.toString()) },
                                name = playlist.playlistEntity.name,
                                artworkUrl = playlist.playlistEntity.artworkUrl
                            )
                        }
                    }
                }
            }
        }
    }
} 