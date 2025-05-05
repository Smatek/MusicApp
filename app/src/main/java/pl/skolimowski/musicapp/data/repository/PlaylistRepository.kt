package pl.skolimowski.musicapp.data.repository

import kotlinx.coroutines.flow.Flow
import pl.skolimowski.musicapp.data.db.dao.PlaylistDao
import pl.skolimowski.musicapp.data.model.Playlist
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.network.AudiusApiService
import pl.skolimowski.musicapp.network.model.PlaylistResponse
import javax.inject.Inject
import javax.inject.Singleton

interface PlaylistRepository {
    fun observePlaylists(): Flow<List<Playlist>>

    fun observePlaylist(playlistId: String): Flow<Playlist?>

    suspend fun fetchAndSaveTrendingPlaylists(): Result<Unit>
}

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val audiusApiService: AudiusApiService,
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> {
        return playlistDao.observePlaylistWithTracks()
    }

    override fun observePlaylist(playlistId: String): Flow<Playlist?> {
        return playlistDao.observePlaylistWithTracks(playlistId)
    }

    override suspend fun fetchAndSaveTrendingPlaylists(): Result<Unit> {
        return try {
            val trendingPlaylistsResponse = audiusApiService.getTrendingPlaylists()
            val playlistEntities = trendingPlaylistsResponse.data.map { mapPlaylistToEntity(it) }
            playlistDao.upsertPlaylists(playlistEntities)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    private fun mapPlaylistToEntity(playlist: PlaylistResponse): PlaylistEntity {
        val artworkUrl = playlist.artwork?.size480
        return PlaylistEntity(
            id = playlist.id,
            name = playlist.playlistName,
            artworkUrl = artworkUrl,
            description = playlist.description,
        )
    }
} 