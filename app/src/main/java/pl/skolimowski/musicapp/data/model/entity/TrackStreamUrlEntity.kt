package pl.skolimowski.musicapp.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.skolimowski.musicapp.data.db.DatabaseConsts

@Entity(
    tableName = DatabaseConsts.TRACK_STREAM_URLS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = TrackInfoEntity::class,
            parentColumns = [DatabaseConsts.TRACK_ID],
            childColumns = [DatabaseConsts.TRACK_STREAM_URL_TRACK_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackStreamUrlEntity(
    @PrimaryKey
    @ColumnInfo(name = DatabaseConsts.TRACK_STREAM_URL_TRACK_ID)
    val trackId: String,
    @ColumnInfo(name = DatabaseConsts.TRACK_STREAM_URL_URL)
    val url: String
) 