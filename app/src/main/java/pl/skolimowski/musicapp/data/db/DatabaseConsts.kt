package pl.skolimowski.musicapp.data.db

object DatabaseConsts {
    // Database
    const val DATABASE_NAME = "music_app_database"
    const val DATABASE_VERSION = 9

    // Tables
    const val TRACKS_TABLE_NAME = "tracks"
    const val TRENDING_TRACKS_TABLE_NAME = "trending_tracks"
    const val TRACK_STREAM_URLS_TABLE_NAME = "track_stream_urls"
    const val RECENTLY_SEARCHED_TRACKS_TABLE_NAME = "recently_searched_tracks"
    const val TABLE_PLAYLISTS = "playlists"
    const val TABLE_PLAYLIST_TRACK_CROSS_REF = "playlist_track_cross_ref"

    // Columns - Track Table
    const val TRACK_ID = "trackId"
    const val TRACK_TITLE = "title"
    const val TRACK_ARTIST = "artist"
    const val TRACK_DURATION = "duration"
    const val ARTWORK_URL_150 = "artwork_url150x150"
    const val ARTWORK_URL_480 = "artwork_url480x480"

    // Columns - TrackStreamUrl Table
    const val TRACK_STREAM_URL_TRACK_ID = "trackId"
    const val TRACK_STREAM_URL_URL = "streamUrl"

    // Columns - TrendingTrack Table
    const val TRENDING_TRACK_TRACK_ID = "trackId"
    const val TRENDING_TRACK_INDEX = "trendingIndex"

    // Columns - RecentlySearchedTrack Table
    const val RECENTLY_SEARCHED_TRACK_ID = "trackId"
    const val RECENTLY_SEARCHED_TIMESTAMP = "searchTimestamp"

    // Columns - Playlist Table
    const val PLAYLIST_ID = "playlistId"
    const val PLAYLIST_NAME = "name"
    const val PLAYLIST_ARTWORK_URL = "artworkUrl"
    const val PLAYLIST_DESCRIPTION = "description"
}