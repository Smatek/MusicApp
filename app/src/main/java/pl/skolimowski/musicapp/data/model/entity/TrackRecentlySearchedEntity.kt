package pl.skolimowski.musicapp.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import pl.skolimowski.musicapp.data.db.DatabaseConsts

@Entity(
    tableName = DatabaseConsts.RECENTLY_SEARCHED_TRACKS_TABLE_NAME,
    foreignKeys = [
        ForeignKey(
            entity = TrackInfoEntity::class,
            parentColumns = [DatabaseConsts.TRACK_ID],
            childColumns = [DatabaseConsts.RECENTLY_SEARCHED_TRACK_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackRecentlySearchedEntity(
    @PrimaryKey
    @ColumnInfo(name = DatabaseConsts.RECENTLY_SEARCHED_TRACK_ID)
    val trackId: String,

    @ColumnInfo(name = DatabaseConsts.RECENTLY_SEARCHED_TIMESTAMP)
    val searchTimestamp: Long
) 