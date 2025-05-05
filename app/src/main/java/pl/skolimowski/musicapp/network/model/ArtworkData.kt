package pl.skolimowski.musicapp.network.model

import com.google.gson.annotations.SerializedName

/**
 * Represents different sizes of artwork URLs (e.g., track artwork, profile pictures).
 */
data class ArtworkData(
    @SerializedName("150x150") val size150: String?,
    @SerializedName("480x480") val size480: String?,
    @SerializedName("1000x1000") val size1000: String?
) 