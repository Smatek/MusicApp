package pl.skolimowski.musicapp.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import pl.skolimowski.musicapp.data.db.DatabaseConsts

@Entity(
    tableName = DatabaseConsts.TRENDING_TRACKS_TABLE_NAME,
    primaryKeys = [DatabaseConsts.TRENDING_TRACK_TRACK_ID, DatabaseConsts.TRENDING_TRACK_INDEX],
    foreignKeys = [
        ForeignKey(
            entity = TrackInfoEntity::class,
            parentColumns = [DatabaseConsts.TRACK_ID],
            childColumns = [DatabaseConsts.TRENDING_TRACK_TRACK_ID],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackTrendingIndexEntity(
    @ColumnInfo(name = DatabaseConsts.TRENDING_TRACK_TRACK_ID)
    val trackId: String,
    @ColumnInfo(name = DatabaseConsts.TRENDING_TRACK_INDEX)
    val index: Int
) 