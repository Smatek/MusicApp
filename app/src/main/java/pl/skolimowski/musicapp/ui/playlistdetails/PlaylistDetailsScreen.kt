package pl.skolimowski.musicapp.ui.playlistdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import pl.skolimowski.musicapp.ui.common.LoadingIllustration
import pl.skolimowski.musicapp.ui.common.TrackListItem

sealed class PlaylistDetailsIntent {
    data object PlayPlaylist : PlaylistDetailsIntent()
}

@Composable
fun PlaylistDetailsScreen(
    viewModel: PlaylistDetailsViewModelInterface = hiltViewModel<PlaylistDetailsViewModel>()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            state.isLoading -> {
                LoadingIllustration(modifier = Modifier.fillMaxSize())
            }

            state.error != null -> {
                Text(
                    text = state.error ?: "An unknown error occurred",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            else -> {
                PlaylistDetailsContent(
                    state = state,
                    onPlayClicked = {
                        viewModel.handleIntent(PlaylistDetailsIntent.PlayPlaylist)
                    }
                )
            }
        }
    }
}

@Composable
private fun PlaylistDetailsContent(
    state: PlaylistDetailsState,
    onPlayClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        state.playlist?.let {
            Text(
                text = it.playlistEntity.name,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Play/Pause Button
            IconButton(
                onClick = onPlayClicked,
                modifier = Modifier.size(72.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircleFilled,
                    contentDescription = "Play",
                    modifier = Modifier.fillMaxSize(),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(state.playlist.tracks, key = { it.trackInfo.id }) { track ->
                    TrackListItem(
                        trackInfo = track.trackInfo,
                        onClick = { /* TODO: Handle track click (e.g., play) */ },
                    )
                }
            }
        }
    }
}