package pl.skolimowski.musicapp.network.model

/**
 * Represents the response from the track search API endpoint.
 */
data class TrackSearchResponse(
    val data: List<TrackData>? = null
) 