package pl.skolimowski.musicapp.data.repository

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.cache.ITrackCacheManager
import pl.skolimowski.musicapp.data.db.dao.PlaylistDao
import pl.skolimowski.musicapp.data.db.dao.TrackDao
import pl.skolimowski.musicapp.data.model.RecentlySearchedTrack
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.data.model.TrendingTrack
import pl.skolimowski.musicapp.data.model.entity.Artwork
import pl.skolimowski.musicapp.data.model.entity.PlaylistTrackCrossRef
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.TrackRecentlySearchedEntity
import pl.skolimowski.musicapp.data.model.entity.TrackStreamUrlEntity
import pl.skolimowski.musicapp.data.model.entity.TrackTrendingIndexEntity
import pl.skolimowski.musicapp.di.DispatcherProvider
import pl.skolimowski.musicapp.network.AudiusApiService
import pl.skolimowski.musicapp.network.model.TrackData
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable? = null) : Result<Nothing>()
}

interface TrackRepository {
    fun observeTrackMatchingQuery(query: String): Flow<List<Track>>

    fun getTrendingTracks(): Flow<List<TrendingTrack>>

    fun getTrackCacheStatusFlow(): StateFlow<Map<Uri, CacheStatus>>

    suspend fun fetchTrendingTracks(): Result<List<TrackInfoEntity>>

    suspend fun getTrack(trackId: String): Result<Track>

    suspend fun getTracks(trackIds: List<String>): Result<List<Track>>

    suspend fun prefetchTrackStart(trackId: String)

    suspend fun prefetchTrackStart(trackIds: Set<String>)

    suspend fun searchTracks(query: String): Result<List<TrackInfoEntity>>

    fun getRecentlySearchedTracks(limit: Int = 10): Flow<List<RecentlySearchedTrack>>

    suspend fun saveRecentlySearchedTrack(trackInfo: TrackInfoEntity)

    suspend fun fetchAndSavePlaylistTracks(playlistId: String): Result<List<TrackInfoEntity>>
}

const val TAG = "TrackRepositoryImpl"

@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val audiusApiService: AudiusApiService,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val trackCacheManager: ITrackCacheManager,
    private val dispatcherProvider: DispatcherProvider
) : TrackRepository {

    override suspend fun fetchTrendingTracks(): Result<List<TrackInfoEntity>> {
        return try {
            val response = audiusApiService.getTrendingTracks()
            val domainTracks = response.data.map { mapTrackDataToDomain(it) }

            saveTrendingTracks(domainTracks)

            Result.Success(domainTracks)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeTrackMatchingQuery(query: String): Flow<List<Track>> {
        return trackDao.observeTracks()
            .map { it.filter { it.trackInfo.title.lowercase().contains(query.lowercase()) } }
    }

    override fun getTrendingTracks(): Flow<List<TrendingTrack>> {
        return trackDao.observeTrendingTracks()
    }

    override suspend fun getTrack(trackId: String): Result<Track> {
        trackDao.observeTrackById(trackId).first()?.let { trackWithDetails ->
            if (trackWithDetails.streamUrl?.url != null) {
                return Result.Success(trackWithDetails)
            } else {
                try {
                    val response = audiusApiService.getTrackStreamUrl(trackId = trackId)
                    val url = response.url

                    return if (url != null) {
                        trackDao.upsertTrackStreamUrl(TrackStreamUrlEntity(trackId, url))
                        val updatedTrack = trackDao.observeTrackById(trackId).first()

                        if (updatedTrack != null) {
                            Result.Success(updatedTrack)
                        } else {
                            Log.e(TAG, "Track $trackId not found after upserting stream URL.")
                            Result.Error(Exception("Track not found after update."))
                        }
                    } else {
                        Log.w(TAG, "Stream URL fetched was null for track $trackId")
                        Result.Error(Exception("Stream URL was null."))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch stream URL for track $trackId", e)
                    return Result.Error(e)
                }
            }
        } ?: run {
            Log.w(TAG, "Track $trackId not found in local database initially.")
            return Result.Error(Exception("Track not found."))
        }
    }

    override suspend fun getTracks(trackIds: List<String>): Result<List<Track>> {
        return withContext(dispatcherProvider.io) {
            val asyncMap = trackIds.map { trackId ->
                async {
                    getTrack(trackId)
                }
            }

            val results = asyncMap.awaitAll()
                .mapNotNull { result ->
                    when (result) {
                        is Result.Success -> result.data
                        is Result.Error -> {
                            Log.e(TAG, "Failed to fetch track: ${result.exception}")
                            null
                        }
                    }
                }

            if (results.size == trackIds.size) {
                return@withContext Result.Success(results)
            } else {
                return@withContext Result.Error(Exception("No tracks found."))
            }
        }
    }

    private suspend fun saveTrendingTracks(trackInfos: List<TrackInfoEntity>) {
        trackDao.upsertTracks(trackInfos)

        val trendingTracks = trackInfos.mapIndexed { index, track ->
            TrackTrendingIndexEntity(index = index, trackId = track.id)
        }

        trackDao.upsertTrendingTracks(trendingTracks)
    }

    override fun getTrackCacheStatusFlow(): StateFlow<Map<Uri, CacheStatus>> {
        return trackCacheManager.cacheStatusFlow
    }

    override suspend fun prefetchTrackStart(trackId: String) {
        when (val result = getTrack(trackId)) {
            is Result.Success -> {
                result.data.streamUrl?.url?.let { url ->
                    try {
                        val uri = url.toUri()
                        Log.d(TAG, "Initiating prefetch for track $trackId, URI: $uri")
                        trackCacheManager.startPreCaching(uri)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse URI or start pre-caching for track $trackId", e)
                    }
                } ?: Log.w(TAG, "Cannot prefetch track $trackId: Stream URL is null.")
            }

            is Result.Error -> {
                Log.w(
                    TAG,
                    "Cannot prefetch track $trackId: Failed to get track details.",
                    result.exception
                )
            }
        }
    }

    override suspend fun prefetchTrackStart(trackIds: Set<String>) {
        withContext(dispatcherProvider.io) {
            trackIds.map { trackId ->
                async { prefetchTrackStart(trackId) }
            }.awaitAll()
        }
    }

    override suspend fun searchTracks(query: String): Result<List<TrackInfoEntity>> {
        return try {
            val response = audiusApiService.searchTracks(query)
            val domainTracks = response.data?.map { mapTrackDataToDomain(it) } ?: emptyList()
            if (domainTracks.isNotEmpty()) {
                trackDao.upsertTracks(domainTracks)
            }
            Result.Success(domainTracks)
        } catch (e: Exception) {
            Log.e(TAG, "Search API call failed for query '$query'", e)
            Result.Error(e)
        }
    }

    override fun getRecentlySearchedTracks(limit: Int): Flow<List<RecentlySearchedTrack>> {
        return trackDao.observeRecentlySearchedTracks()
            .map { list -> list.filter { track -> track.searchTimestamp != null } }
    }

    override suspend fun saveRecentlySearchedTrack(trackInfo: TrackInfoEntity) {
        val recentTrack = TrackRecentlySearchedEntity(
            trackId = trackInfo.id,
            searchTimestamp = System.currentTimeMillis()
        )
        trackDao.upsertTrackRecentlySearched(recentTrack)
    }

    override suspend fun fetchAndSavePlaylistTracks(playlistId: String): Result<List<TrackInfoEntity>> {
        return try {
            val response = audiusApiService.getPlaylistTracks(playlistId)
            val domainTracks = response.data.map { mapTrackDataToDomain(it) }

            if (domainTracks.isNotEmpty()) {
                trackDao.upsertTracks(domainTracks)

                playlistDao.addTrackToPlaylist(
                    domainTracks.map {
                        PlaylistTrackCrossRef(
                            playlistId = playlistId,
                            trackId = it.id
                        )
                    }
                )
            }

            Result.Success(domainTracks)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch or save playlist tracks for playlistId: $playlistId", e)
            Result.Error(e)
        }
    }

    private fun mapTrackDataToDomain(trackData: TrackData): TrackInfoEntity {
        // Create Artwork object from network response
        val artwork =
            if (trackData.artwork?.size150 != null || trackData.artwork?.size480 != null) {
                Artwork(
                    url150x150 = trackData.artwork.size150,
                    url480x480 = trackData.artwork.size480
                )
            } else {
                null // Set artwork to null if neither size is available
            }

        return TrackInfoEntity(
            id = trackData.id,
            title = trackData.title,
            artist = trackData.user.name,
            duration = trackData.duration * 1000L, // Convert seconds to milliseconds
            artwork = artwork // Assign the mapped Artwork object
        )
    }
}