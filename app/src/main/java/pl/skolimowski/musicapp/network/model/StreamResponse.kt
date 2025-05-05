package pl.skolimowski.musicapp.network.model

import com.google.gson.annotations.SerializedName

data class StreamUrlResponse(
    @SerializedName("data") val url: String?
) 