package pl.skolimowski.musicapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.skolimowski.musicapp.data.db.DatabaseConsts
import pl.skolimowski.musicapp.data.model.PlaylistImpl
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.model.entity.PlaylistTrackCrossRef

@Dao
interface PlaylistDao {
    @Upsert
    suspend fun upsertPlaylists(playlists: List<PlaylistEntity>)

    @Upsert
    suspend fun addTrackToPlaylist(crossRefs: List<PlaylistTrackCrossRef>)

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TABLE_PLAYLISTS}")
    fun observePlaylistWithTracks(): Flow<List<PlaylistImpl>>

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TABLE_PLAYLISTS} WHERE `${DatabaseConsts.PLAYLIST_ID}` = :playListId")
    fun observePlaylistWithTracks(playListId: String): Flow<PlaylistImpl?>
}