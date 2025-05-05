package pl.skolimowski.musicapp.network

import pl.skolimowski.musicapp.network.model.StreamUrlResponse
import pl.skolimowski.musicapp.network.model.TrendingTracksResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import pl.skolimowski.musicapp.network.model.TrackSearchResponse
import pl.skolimowski.musicapp.network.model.TrendingPlaylistsResponse
import pl.skolimowski.musicapp.network.model.PlaylistTracksResponse

interface AudiusApiService {

    /**
     * Gets the top 100 trending (most popular) tracks on Audius.
     * See: https://docs.audius.org/developers/api/get-trending-tracks
     *
     * @param genre Filter trending to a specified genre (Optional).
     * @param time Calculate trending over a specified time range: 'week', 'month', 'year', 'allTime' (Optional).
     * @return A response object containing a list of trending tracks.
     */
    @GET("tracks/trending") // Relative path to the base URL
    suspend fun getTrendingTracks(
        @Query("genre") genre: String? = null,
        @Query("time") time: String? = null
    ): TrendingTracksResponse

    /**
     * Gets the streamable MP3 file URL of a track.
     * See: https://docs.audius.org/developers/api/stream-track
     *
     * @param trackId The ID of the track to stream.
     * @param noRedirect If true, returns the stream URL in JSON instead of a 302 redirect.
     * @return A response object containing the stream URL.
     */
    @GET("tracks/{track_id}/stream")
    suspend fun getTrackStreamUrl(
        @Path("track_id") trackId: String,
        @Query("no_redirect") noRedirect: Boolean = true, // Request JSON response
        @Query("app_name") appName: String = "MusicApp/1.0"
    ): StreamUrlResponse

    // Add search endpoint
    @GET("tracks/search")
    suspend fun searchTracks(
        @Query("query") query: String,
        @Query("app_name") appName: String = "MusicApp/1.0"
    ): TrackSearchResponse

    /**
     * Gets trending playlists on Audius.
     * See: https://audiusproject.github.io/api-docs/?http#get-trending-playlists
     *
     * @param time Calculate trending over a specified time range: 'week', 'month', 'year', 'allTime' (Optional).
     * @return A response object containing a list of trending playlists.
     */
    @GET("playlists/trending") // Using v1 implicitly via base URL likely
    suspend fun getTrendingPlaylists(
        @Query("time") time: String? = null,
        @Query("app_name") appName: String = "MusicApp/1.0"
    ): TrendingPlaylistsResponse

    /**
     * Gets the tracks for a specific playlist.
     * See: https://audiusproject.github.io/api-docs/?http#get-playlist-tracks
     *
     * @param playlistId The ID of the playlist.
     * @param appName The name of the application making the request.
     * @return A response object containing a list of tracks in the playlist.
     */
    @GET("playlists/{playlist_id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("playlist_id") playlistId: String,
        @Query("app_name") appName: String = "MusicApp/1.0"
    ): PlaylistTracksResponse
} 