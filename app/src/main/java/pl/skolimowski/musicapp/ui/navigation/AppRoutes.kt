package pl.skolimowski.musicapp.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoutes {
    @Serializable
    data object TrendingRoute : AppRoutes()

    @Serializable
    data object SearchRoute : AppRoutes()

    @Serializable
    data object PlaylistRoute : AppRoutes()

    @Serializable
    data class PlaylistDetailsRoute(val playlistId: String)
}