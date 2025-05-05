package pl.skolimowski.musicapp.network.model

import com.google.gson.annotations.SerializedName

data class PlaylistTracksResponse(
    @SerializedName("data") val data: List<TrackData>
) 