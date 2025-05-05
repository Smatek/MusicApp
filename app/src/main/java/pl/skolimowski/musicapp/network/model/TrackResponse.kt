package pl.skolimowski.musicapp.network.model

import com.google.gson.annotations.SerializedName

// Top-level response structure
data class TrendingTracksResponse(
    @SerializedName("data") val data: List<TrackData>
)

// Represents a single track item in the response
data class TrackData(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("user") val user: UserData,
    @SerializedName("artwork") val artwork: ArtworkData?,
    @SerializedName("genre") val genre: String,
    @SerializedName("mood") val mood: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("repost_count") val repostCount: Int,
    @SerializedName("favorite_count") val favoriteCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("play_count") val playCount: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("tags") val tags: String?,
    @SerializedName("permalink") val permalink: String,
    @SerializedName("is_streamable") val isStreamable: Boolean?,
    @SerializedName("is_downloadable") val isDownloadable: Boolean,
    @SerializedName("track_cid") val trackCid: String?,
    @SerializedName("preview_cid") val previewCid: String?,
    @SerializedName("orig_file_cid") val origFileCid: String?,
    @SerializedName("orig_filename") val origFilename: String?,
    @SerializedName("is_original_available") val isOriginalAvailable: Boolean,
    @SerializedName("remix_of") val remixOf: RemixOfData?,
    @SerializedName("playlists_containing_track") val playlistsContainingTrack: List<Int>?, // Assuming IDs are integers
    @SerializedName("ddex_app") val ddexApp: String?,
    @SerializedName("pinned_comment_id") val pinnedCommentId: Int?

)

// Represents the user data nested within a track
data class UserData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("handle") val handle: String,
    @SerializedName("bio") val bio: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("profile_picture") val profilePicture: ArtworkData?,
    @SerializedName("cover_photo") val coverPhoto: CoverPhotoData?,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("track_count") val trackCount: Int,
    @SerializedName("playlist_count") val playlistCount: Int,
    @SerializedName("follower_count") val followerCount: Int,
    @SerializedName("followee_count") val followeeCount: Int,
    @SerializedName("repost_count") val repostCount: Int,
    @SerializedName("album_count") val albumCount: Int,
    @SerializedName("artist_pick_track_id") val artistPickTrackId: String?,
    @SerializedName("twitter_handle") val twitterHandle: String?,
    @SerializedName("instagram_handle") val instagramHandle: String?,
    @SerializedName("tiktok_handle") val tiktokHandle: String?,
    @SerializedName("website") val website: String?,
    @SerializedName("donation") val donation: String?,
    @SerializedName("wallet") val wallet: String,
    @SerializedName("erc_wallet") val ercWallet: String,
    @SerializedName("spl_wallet") val splWallet: String,
    @SerializedName("spl_usdc_payout_wallet") val splUsdcPayoutWallet: String?,
    @SerializedName("supporter_count") val supporterCount: Int,
    @SerializedName("supporting_count") val supportingCount: Int,
    @SerializedName("total_audio_balance") val totalAudioBalance: Long,
    @SerializedName("is_deactivated") val isDeactivated: Boolean,
    @SerializedName("is_available") val isAvailable: Boolean
)

// Represents cover photo URLs
data class CoverPhotoData(
    @SerializedName("640x") val size640: String?,
    @SerializedName("2000x") val size2000: String?
)

// Represents the remix information
data class RemixOfData(
    @SerializedName("tracks") val tracks: List<RemixTrackInfo>?
)

// Represents the parent track info within remix_of
data class RemixTrackInfo(
    @SerializedName("parent_track_id") val parentTrackId: String
) 