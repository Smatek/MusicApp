package pl.skolimowski.musicapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import pl.skolimowski.musicapp.data.db.DatabaseConsts
import pl.skolimowski.musicapp.data.model.RecentlySearchedTrackImpl
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.TrackImpl
import pl.skolimowski.musicapp.data.model.entity.TrackRecentlySearchedEntity
import pl.skolimowski.musicapp.data.model.entity.TrackStreamUrlEntity
import pl.skolimowski.musicapp.data.model.entity.TrackTrendingIndexEntity
import pl.skolimowski.musicapp.data.model.TrendingTrackImpl

@Dao
interface TrackDao {
    @Upsert
    suspend fun upsertTracks(trackInfos: List<TrackInfoEntity>)

    @Upsert
    suspend fun upsertTrackStreamUrl(trackStreamUrl: TrackStreamUrlEntity)

    @Upsert
    suspend fun upsertTrackRecentlySearched(trackRecentlySearched: TrackRecentlySearchedEntity)

    @Upsert
    suspend fun upsertTrendingTracks(trendingTracks: List<TrackTrendingIndexEntity>)

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TRACKS_TABLE_NAME}")
    fun observeTracks(): Flow<List<TrackImpl>>

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TRACKS_TABLE_NAME} WHERE `${DatabaseConsts.TRACK_ID}` = :trackId")
    fun observeTrackById(trackId: String): Flow<TrackImpl?>

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TRACKS_TABLE_NAME}")
    fun observeTrendingTracks(): Flow<List<TrendingTrackImpl>>

    @Transaction
    @Query("SELECT * FROM ${DatabaseConsts.TRACKS_TABLE_NAME}")
    fun observeRecentlySearchedTracks(): Flow<List<RecentlySearchedTrackImpl>>
}