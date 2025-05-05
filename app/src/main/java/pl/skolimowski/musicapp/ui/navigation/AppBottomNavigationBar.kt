package pl.skolimowski.musicapp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: AppRoutes, val icon: ImageVector, val label: String) {
    data object Trending : BottomNavItem(AppRoutes.TrendingRoute, Icons.Filled.Star, "Trending")
    data object Search : BottomNavItem(AppRoutes.SearchRoute, Icons.Filled.Search, "Search")
    data object Playlist : BottomNavItem(AppRoutes.PlaylistRoute, Icons.Filled.PlaylistPlay, "Playlist")
}

private val bottomNavItems = listOf(BottomNavItem.Trending, BottomNavItem.Search, BottomNavItem.Playlist)

@Composable
fun AppBottomNavigationBar(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onItemClick: (AppRoutes) -> Unit
) {
    NavigationBar(
        modifier = modifier
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route::class.qualifiedName

            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selected,
                onClick = { onItemClick(item.route) }
            )
        }
    }
} 