@file:OptIn(ExperimentalSharedTransitionApi::class)

package pl.skolimowski.musicapp.ui.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.common.CoverPhoto
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalAnimationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun Player(
    modifier: Modifier = Modifier,
    viewModel: IPlayerViewModel
) {
    val state by viewModel.state.collectAsState()

    AnimatedVisibility(
        visible = state.currentTrackInfo != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut()
    ) {
        SharedTransitionLayout {
            AnimatedContent(
                targetState = state.isExpanded,
                modifier = modifier,
                label = "PlayerExpansion"
            ) { isExpanded ->
                BackHandler(enabled = isExpanded) {
                    viewModel.sendIntent(PlayerIntent.ToggleExpanded(isExpanded = false))
                }

                if (isExpanded) {
                    ExpandedPlayer(
                        trackInfo = state.currentTrackInfo!!,
                        progress = state.progress,
                        isPlaying = state.isPlaying,
                        onPlayPauseClick = { viewModel.sendIntent(PlayerIntent.PlayPauseClicked) },
                        onSeekBarPositionChanged = { positionFraction ->
                            viewModel.sendIntent(PlayerIntent.SeekPosition(positionFraction))
                        },
                        onSeekBarPositionFinallyChanged = {
                            viewModel.sendIntent(PlayerIntent.SeekPositionFinally)
                        },
                        onRewindClick = { viewModel.sendIntent(PlayerIntent.RewindClicked) },
                        onForwardClick = { viewModel.sendIntent(PlayerIntent.ForwardClicked) },
                        onPreviousClick = { viewModel.sendIntent(PlayerIntent.PreviousClicked) },
                        onNextClick = { viewModel.sendIntent(PlayerIntent.NextClicked) },
                        isNextEnabled = state.hasNextTrack,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                } else {
                    CollapsedPlayer(
                        state = state,
                        onPlayPauseClick = { viewModel.sendIntent(PlayerIntent.PlayPauseClicked) },
                        onClick = { viewModel.sendIntent(PlayerIntent.ToggleExpanded(isExpanded = true)) },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CollapsedPlayer(
    state: PlayerState,
    onClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val currentTrack = state.currentTrackInfo // Assume not null if CollapsedPlayer is shown

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick), // Click whole surface to expand
        shadowElevation = 4.dp,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // Ensure row fits content height
                    .padding(horizontal = 8.dp, vertical = 4.dp), // Reduced vertical padding
                verticalAlignment = Alignment.CenterVertically
            ) {
                with(sharedTransitionScope) {
                    CoverPhoto(
                        imageUrl = currentTrack?.artwork?.url150x150,
                        contentDescription = "Album cover",
                        size = 48.dp,
                        modifier = Modifier
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "album-cover"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .clip(MaterialTheme.shapes.small)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "title-artist"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .weight(1f), // Takes remaining space
                        verticalArrangement = Arrangement.Center // Center text vertically
                    ) {
                        Text(
                            text = currentTrack?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentTrack?.artist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = {
                        // Stop propagation to the Row's onClick
                        onPlayPauseClick()
                    }) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedPlayer(
    trackInfo: TrackInfoEntity,
    progress: Float,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeekBarPositionChanged: (Float) -> Unit,
    onSeekBarPositionFinallyChanged: () -> Unit,
    onRewindClick: () -> Unit,
    onForwardClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isNextEnabled: Boolean,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Surface(
        modifier = modifier.fillMaxSize(), // Fill the available space
        color = MaterialTheme.colorScheme.background // Use background color
    ) {
        with(sharedTransitionScope) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp)) // Space from top/collapse button

                // Album Art
                CoverPhoto(
                    imageUrl = trackInfo.artwork?.url480x480,
                    contentDescription = "Album art for ${trackInfo.title}",
                    modifier = Modifier
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = "album-cover"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .fillMaxWidth(0.8f) // Take 80% width
                        .aspectRatio(1f)
                        .clip(MaterialTheme.shapes.large) // Larger rounding
                )

                Spacer(Modifier.height(16.dp))

                // Title and Artist Container for sharedBounds
                Column(
                    modifier = Modifier
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "title-artist"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                        .fillMaxWidth(), // Ensure bounds take full width
                    horizontalAlignment = Alignment.CenterHorizontally // Center text within the bounds
                ) {
                    Text(
                        text = trackInfo.title,
                        style = MaterialTheme.typography.headlineMedium, // Larger title
                        textAlign = TextAlign.Center,
                        maxLines = 2, // Allow two lines for title
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = trackInfo.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val interactionSource = remember { MutableInteractionSource() }
                Slider(
                    value = progress,
                    onValueChange = { onSeekBarPositionChanged(it) },
                    onValueChangeFinished = { onSeekBarPositionFinallyChanged() },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.systemGestures.only(WindowInsetsSides.Horizontal)),
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                    },
                    track = { sliderPositions ->
                        SliderDefaults.Track(
                            sliderState = sliderPositions,
                            thumbTrackGapSize = 0.dp,
                            modifier = Modifier.height(4.dp),
                            drawStopIndicator = null
                        )
                    }
                )

                // Time Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp), // Add padding to align text with slider ends
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentPositionMs = (progress * trackInfo.duration).toLong()
                    Text(
                        text = formatTime(currentPositionMs),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(text = formatTime(trackInfo.duration), style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(24.dp)) // More space before controls

                // Control Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    IconButton(
                        onClick = onPreviousClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "Previous track",
                            modifier = Modifier.fillMaxSize(0.7f),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Rewind Button
                    IconButton(
                        onClick = onRewindClick,
                        modifier = Modifier.size(56.dp) // Slightly smaller than play/pause
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            modifier = Modifier.fillMaxSize(0.7f), // Adjust icon size within button
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Play/Pause Button
                    IconButton(
                        onClick = onPlayPauseClick,
                        modifier = Modifier.size(72.dp) // Large central button
                    ) {
                        Icon(
                            imageVector = if (isPlaying)
                                Icons.Filled.PauseCircleFilled
                            else
                                Icons.Filled.PlayCircleFilled,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Forward Button
                    IconButton(
                        onClick = onForwardClick,
                        modifier = Modifier.size(56.dp) // Slightly smaller than play/pause
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Forward10,
                            contentDescription = "Forward 10 seconds",
                            modifier = Modifier.fillMaxSize(0.7f), // Adjust icon size within button
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Next Button
                    val nextButtonColor = if (isNextEnabled) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    IconButton(
                        onClick = onNextClick,
                        enabled = isNextEnabled,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "Next track",
                            modifier = Modifier.fillMaxSize(0.7f),
                            tint = nextButtonColor
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f)) // Push controls up if needed
            }
        }
    }
}

// Helper function to format milliseconds to MM:SS (copied from DetailsScreen)
private fun formatTime(milliseconds: Long): String {
    if (milliseconds < 0) return "00:00" // Handle potential negative values if needed
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
            TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}