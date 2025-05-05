package pl.skolimowski.musicapp.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopAppBar(
    currentRoute: String?,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit,
    onCollapseClick: () -> Unit,
    isPlayerExpanded: Boolean = false,
) {
    val title = when {
        currentRoute?.startsWith(AppRoutes.TrendingRoute::class.qualifiedName!!) == true -> "Trending"
        currentRoute?.startsWith(AppRoutes.SearchRoute::class.qualifiedName!!) == true -> "Search"
        currentRoute?.startsWith(AppRoutes.PlaylistRoute::class.qualifiedName!!) == true -> "Playlists"
        currentRoute?.startsWith(AppRoutes.PlaylistDetailsRoute::class.qualifiedName!!) == true -> "Playlist Details"
        else -> "Music App"
    }

    TopAppBar(
        title = {
            if (!isPlayerExpanded) {
                Text(title)
            }
        },
        modifier = modifier,
        navigationIcon = {
            when {
                isPlayerExpanded -> {
                    IconButton(onClick = onCollapseClick) {
                        Icon(
                            modifier = Modifier.size(48.dp),
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Collapse Player",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant // Use a less prominent color
                        )
                    }
                }
                currentRoute?.startsWith(AppRoutes.PlaylistDetailsRoute::class.qualifiedName!!) == true -> {
                    IconButton(onClick = onNavigationIconClick) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        }
    )
} 