package pl.skolimowski.musicapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pl.skolimowski.musicapp.R
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme
import java.util.concurrent.TimeUnit

@Composable
fun TrackListItem(
    trackInfo: TrackInfoEntity,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CoverPhoto(
                imageUrl = trackInfo.artwork?.url150x150,
                contentDescription = stringResource(R.string.cd_album_cover, trackInfo.title),
                size = 56.dp,
                modifier = Modifier.clip(MaterialTheme.shapes.small)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = trackInfo.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatDuration(trackInfo.duration),
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                shape = CircleShape
                            ),
                    )

                    Text(
                        text = trackInfo.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

// Helper function to format milliseconds to mm:ss
fun formatDuration(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Preview(showBackground = true)
@Composable
private fun PreviewTrackListItem() {
    val trackInfo = TrackInfoEntity(
        id = "1",
        title = "A Great Song Title",
        artist = "The Best Artist",
        duration = 215000,
        artwork = Artwork(url150x150 = null, url480x480 = null)
    )
    MusicAppTheme {
        TrackListItem(
            trackInfo = trackInfo,
            onClick = {},
        )
    }
} 