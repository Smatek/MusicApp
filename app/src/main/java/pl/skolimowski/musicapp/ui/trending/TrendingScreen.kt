package pl.skolimowski.musicapp.ui.trending

import android.util.Log
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import pl.skolimowski.musicapp.R
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.TrendingTrack
import pl.skolimowski.musicapp.data.model.TrendingTrackImpl
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.TrackStreamUrlEntity
import pl.skolimowski.musicapp.ui.common.FullScreenMessage
import pl.skolimowski.musicapp.ui.common.TrackListItem
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme
import pl.skolimowski.musicapp.ui.trending.TrendingScreenIntent.RefreshData
import pl.skolimowski.musicapp.ui.trending.TrendingScreenIntent.TrackClicked
import pl.skolimowski.musicapp.ui.trending.TrendingScreenIntent.VisibleTracksChanged

@Composable
fun TrendingScreen(
    viewModel: ITrendingScreenViewModel,
    onTrackClicked: (TrendingTrack) -> Unit // Callback for navigation
) {
    val state by viewModel.state.collectAsState()

    // Collect side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is TrendingScreenSideEffect.NavigateToDetails -> {
                    onTrackClicked(effect.trendingTrack)
                }
            }
        }
    }

    TrendingScreenContent(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun TrendingScreenContent(
    state: TrendingScreenState,
    onIntent: (TrendingScreenIntent) -> Unit
) {
    val isLoading = state.screenState is TrendingScreenStates.Loading
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState, state.trendingTracks) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .mapNotNull { visibleItems ->
                if (visibleItems.isEmpty()) return@mapNotNull null
                visibleItems.mapNotNull { state.trendingTracks.getOrNull(it.index)?.trackInfo?.id }
            }
            .map { it.toSet() }
            .distinctUntilChanged()
            .debounce(300L)
            .collectLatest { visibleTrackIds ->
                onIntent(VisibleTracksChanged(visibleTrackIds))
            }
    }

    Log.d("TrendingScreen", "TrendingScreenContent: ${state.screenState}")
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = { onIntent(RefreshData) },
        modifier = Modifier.fillMaxSize()
    ) {
        when (state.screenState) {
            is TrendingScreenStates.Loading -> {
                // empty handled by pull refresh indicator
            }

            is TrendingScreenStates.Success -> {
                if (state.trendingTracks.isEmpty()) {
                    // Optional: Show empty state message
                    FullScreenMessage(message = stringResource(id = R.string.trending_empty))
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(state.trendingTracks, key = { it.trackInfo.id }) { track ->
                            TrackListItem(
                                trackInfo = track.trackInfo,
                                onClick = { onIntent(TrackClicked(track)) },
                            )
                        }
                    }
                }
            }

            is TrendingScreenStates.Error -> {
                when (state.screenState.errorType) {
                    ErrorType.NO_NETWORK -> FullScreenMessage(
                        message = stringResource(id = R.string.error_no_network),
                        // Optional: Add drawable resource for no network icon
                        // iconResId = R.drawable.ic_no_network
                    )

                    ErrorType.SERVER_ERROR -> FullScreenMessage(
                        message = stringResource(id = R.string.error_server),
                        // Optional: Add drawable resource for server error icon
                        // iconResId = R.drawable.ic_server_error
                    )
                }
            }

            is TrendingScreenStates.Uninitialized -> {
                // empty
            }
        }
    }
}

// --- Previews --- //

@Preview(showBackground = true)
@Composable
private fun PreviewTrendingScreenSuccess() {
    val mockTrackInfos = listOf(
        TrendingTrackImpl(TrackInfoEntity("1", "Blinding Lights", "The Weeknd", 200000), TrackStreamUrlEntity("1", "url")),
        TrendingTrackImpl(TrackInfoEntity("2", "Shape of You", "Ed Sheeran", 233000), TrackStreamUrlEntity("2", "url")),
        TrendingTrackImpl(TrackInfoEntity(
            "3",
            "Someone You Loved - Extra Long Title That Needs to Ellipsize",
            "Lewis Capaldi - Artist Also Very Long",
            182000
        ), TrackStreamUrlEntity("3", "url"))
    )
    MusicAppTheme {
        TrendingScreenContent(
            state = TrendingScreenState(
                screenState = TrendingScreenStates.Success,
                trendingTracks = mockTrackInfos
            ),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTrendingScreenLoading() {
    MusicAppTheme {
        TrendingScreenContent(
            state = TrendingScreenState(TrendingScreenStates.Loading),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "No Network Error")
@Composable
private fun PreviewTrendingScreenErrorNoNetwork() {
    MusicAppTheme {
        TrendingScreenContent(
            state = TrendingScreenState(TrendingScreenStates.Error(ErrorType.NO_NETWORK)),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true, name = "Server Error")
@Composable
private fun PreviewTrendingScreenErrorServer() {
    MusicAppTheme {
        TrendingScreenContent(
            state = TrendingScreenState(TrendingScreenStates.Error(ErrorType.SERVER_ERROR)),
            onIntent = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTrackListItem() {
    val trackInfo = TrackInfoEntity(
        id = "1",
        title = "A Great Song Title",
        artist = "The Best Artist",
        duration = 215000,
        artwork = null
    )
    MusicAppTheme {
        pl.skolimowski.musicapp.ui.common.TrackListItem(
            trackInfo = trackInfo,
            onClick = {},
        )
    }
} 