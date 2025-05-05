package pl.skolimowski.musicapp.network.model

import com.google.gson.annotations.SerializedName

data class TrendingPlaylistsResponse(
    @SerializedName("data") val data: List<PlaylistResponse>
)

data class PlaylistResponse(
    @SerializedName("artwork") val artwork: ArtworkData?,
    @SerializedName("description") val description: String?,
    @SerializedName("id") val id: String,
    @SerializedName("is_album") val isAlbum: Boolean,
    @SerializedName("playlist_name") val playlistName: String,
    @SerializedName("repost_count") val repostCount: Int,
    @SerializedName("favorite_count") val favoriteCount: Int,
    @SerializedName("total_play_count") val totalPlayCount: Int,
    @SerializedName("user") val user: PlaylistUser
)

data class PlaylistUser(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("handle") val handle: String,
    @SerializedName("is_verified") val isVerified: Boolean
) 