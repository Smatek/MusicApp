package pl.skolimowski.musicapp.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pl.skolimowski.musicapp.data.db.DatabaseConsts

@Entity(tableName = DatabaseConsts.TABLE_PLAYLISTS)
data class PlaylistEntity(
    @PrimaryKey
    @ColumnInfo(name = DatabaseConsts.PLAYLIST_ID)
    val id: String,

    @ColumnInfo(name = DatabaseConsts.PLAYLIST_NAME)
    val name: String,

    @ColumnInfo(name = DatabaseConsts.PLAYLIST_ARTWORK_URL)
    val artworkUrl: String?,

    @ColumnInfo(name = DatabaseConsts.PLAYLIST_DESCRIPTION)
    val description: String?,
)

@Entity(
    tableName = DatabaseConsts.TABLE_PLAYLIST_TRACK_CROSS_REF,
    primaryKeys = [DatabaseConsts.PLAYLIST_ID, DatabaseConsts.TRACK_ID],
)
data class PlaylistTrackCrossRef(
    @ColumnInfo(name = DatabaseConsts.PLAYLIST_ID)
    val playlistId: String,
    @ColumnInfo(name = DatabaseConsts.TRACK_ID, index = true)
    val trackId: String
)