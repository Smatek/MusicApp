package pl.skolimowski.musicapp.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import pl.skolimowski.musicapp.data.db.DatabaseConsts
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.model.entity.PlaylistTrackCrossRef
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity

interface Playlist {
    val playlistEntity: PlaylistEntity
    val tracks: List<Track>
}

open class PlaylistImpl(
    @Embedded override val playlistEntity: PlaylistEntity,

    @Relation(
        parentColumn = DatabaseConsts.PLAYLIST_ID,
        entity = TrackInfoEntity::class, // Target TrackImpl
        entityColumn = DatabaseConsts.TRACK_ID,
        associateBy = Junction(
            value = PlaylistTrackCrossRef::class,
            parentColumn = DatabaseConsts.PLAYLIST_ID,
            entityColumn = DatabaseConsts.TRACK_ID
        )
    )
    val playlistTracks: List<TrackImpl> = emptyList()
) : Playlist {
    override val tracks: List<Track>
        get() = playlistTracks
}