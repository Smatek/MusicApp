package pl.skolimowski.musicapp.ui.search

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import pl.skolimowski.musicapp.R
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.ui.common.FullScreenMessage
import pl.skolimowski.musicapp.ui.common.TrackListItem

sealed class SearchScreenIntent {
    data class SearchQueryChanged(val query: String) : SearchScreenIntent()
    data class TrackClicked(val track: Track) : SearchScreenIntent()
    data class VisibleTracksChanged(val visibleTrackIds: Set<String>) : SearchScreenIntent()
}

sealed class SearchScreenSideEffect {
    data class NavigateToDetails(val track: Track) : SearchScreenSideEffect()
}

@Composable
fun SearchScreen(
    viewModel: ISearchScreenViewModel,
    onTrackClicked: (Track) -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SearchScreenSideEffect.NavigateToDetails -> {
                    onTrackClicked(effect.track)
                }
            }
        }
    }

    SearchScreenContent(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@OptIn(FlowPreview::class)
@Composable
fun SearchScreenContent(
    state: SearchScreenState,
    onIntent: (SearchScreenIntent) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Remember the text field state across recompositions
    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(state.query))
    }


    // Effect to update ViewModel when TextFieldValue changes (debounced implicitly by VM)
    LaunchedEffect(textFieldValue) {
        if (textFieldValue.text != state.query) { // Avoid redundant updates
            onIntent(SearchScreenIntent.SearchQueryChanged(textFieldValue.text))
        }
    }

    LaunchedEffect(lazyListState, state.searchResults, state.recentSearches) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .mapNotNull { visibleItems ->
                if (visibleItems.isEmpty()) return@mapNotNull null
                val currentList =
                    if (state.query.isBlank()) state.recentSearches else state.searchResults
                if (currentList.isEmpty()) return@mapNotNull null

                visibleItems.mapNotNull { itemInfo ->
                    currentList.getOrNull(itemInfo.index)?.trackInfo?.id
                }
            }
            .map { it.toSet() }
            .distinctUntilChanged()
            .debounce(300L) // Debounce prefetch requests
            .collectLatest { visibleTrackIds ->
                if (visibleTrackIds.isNotEmpty()) {
                    Log.d("SearchScreen", "Visible track IDs for pre-cache: $visibleTrackIds")
                    onIntent(SearchScreenIntent.VisibleTracksChanged(visibleTrackIds))
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { textFieldValue = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("search") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(onClick = { textFieldValue = TextFieldValue("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "clear")
                    }
                }
            },
            singleLine = true
        )

        Box(modifier = Modifier.weight(1f)) {
            when (state.screenState) {
                is SearchScreenStateEnum.Error -> {
                    val message = when (state.screenState.errorType) {
                        SearchErrorType.NO_NETWORK -> stringResource(id = R.string.error_no_network)
                        SearchErrorType.SERVER_ERROR -> stringResource(id = R.string.error_server)
                    }
                    FullScreenMessage(message = message)
                }

                is SearchScreenStateEnum.Idle -> {
                    if (state.recentSearches.isEmpty()) {
                        FullScreenMessage(message = "search recent empty")
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp) // Padding at the bottom
                        ) {
                            item { // Header for recent searches
                                Text(
                                    text = "search recent header",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            items(state.recentSearches, key = { it.trackInfo.id }) { recentSearched ->
                                TrackListItem(
                                    trackInfo = recentSearched.trackInfo,
                                    onClick = {
                                        onIntent(
                                            SearchScreenIntent.TrackClicked(
                                                recentSearched
                                            )
                                        )
                                    },
                                )
                            }
                        }
                    }
                }

                is SearchScreenStateEnum.Search -> {
                    if (state.searchResults.isEmpty()) {
                        FullScreenMessage(message = "no result for ${state.query}")
                    } else {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 8.dp)
                        ) {
                            items(state.searchResults, key = { it.trackInfo.id }) { track ->
                                TrackListItem(
                                    trackInfo = track.trackInfo,
                                    onClick = {
                                        keyboardController?.hide() // Hide keyboard on click
                                        onIntent(SearchScreenIntent.TrackClicked(track))
                                    },
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLoading) {
                // todo make it prettier
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}