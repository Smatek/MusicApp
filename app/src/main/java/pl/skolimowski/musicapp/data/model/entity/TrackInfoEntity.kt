package pl.skolimowski.musicapp.data.model.entity

import androidx.media3.common.MediaItem
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import pl.skolimowski.musicapp.data.db.DatabaseConsts

@Serializable
data class Artwork(
    @ColumnInfo(name = DatabaseConsts.ARTWORK_URL_150)
    val url150x150: String?,
    @ColumnInfo(name = DatabaseConsts.ARTWORK_URL_480)
    val url480x480: String?
)

@Entity(tableName = DatabaseConsts.TRACKS_TABLE_NAME)
@Serializable
data class TrackInfoEntity(
    @PrimaryKey
    @ColumnInfo(name = DatabaseConsts.TRACK_ID)
    val id: String,
    @ColumnInfo(name = DatabaseConsts.TRACK_TITLE)
    val title: String,
    @ColumnInfo(name = DatabaseConsts.TRACK_ARTIST)
    val artist: String,
    @ColumnInfo(name = DatabaseConsts.TRACK_DURATION)
    val duration: Long,
    @Embedded(prefix = "artwork_") // Add prefix to avoid column name clashes if needed
    val artwork: Artwork? = null
)

fun MediaItem.toTrackInfo(durationMs: Long): TrackInfoEntity {
    val artworkUriString = mediaMetadata.artworkUri?.toString()

    val artwork = artworkUriString?.let {
        Artwork(url150x150 = it, url480x480 = it)
    }
    return TrackInfoEntity(
        id = mediaId,
        title = mediaMetadata.title?.toString() ?: "Unknown Title",
        artist = mediaMetadata.artist?.toString() ?: "Unknown Artist",
        duration = durationMs,
        artwork = artwork
    )
}