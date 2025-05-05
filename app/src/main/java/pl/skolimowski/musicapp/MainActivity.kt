package pl.skolimowski.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.skolimowski.musicapp.ui.navigation.AppBottomNavigationBar
import pl.skolimowski.musicapp.ui.navigation.AppRoutes
import pl.skolimowski.musicapp.ui.navigation.AppTopAppBar
import pl.skolimowski.musicapp.ui.player.IPlayerViewModel
import pl.skolimowski.musicapp.ui.player.Player
import pl.skolimowski.musicapp.ui.player.PlayerIntent
import pl.skolimowski.musicapp.ui.player.PlayerViewModel
import pl.skolimowski.musicapp.ui.playlist.PlaylistScreen
import pl.skolimowski.musicapp.ui.playlist.PlaylistViewModel
import pl.skolimowski.musicapp.ui.playlistdetails.PlaylistDetailsScreen
import pl.skolimowski.musicapp.ui.search.ISearchScreenViewModel
import pl.skolimowski.musicapp.ui.search.SearchScreen
import pl.skolimowski.musicapp.ui.search.SearchScreenViewModel
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme
import pl.skolimowski.musicapp.ui.trending.ITrendingScreenViewModel
import pl.skolimowski.musicapp.ui.trending.TrendingScreen
import pl.skolimowski.musicapp.ui.trending.TrendingScreenViewModel

// Define sealed class for intents
sealed class MainActivityIntent {
    data object RegisterNetworkCallback : MainActivityIntent()
    data object UnregisterNetworkCallback : MainActivityIntent()
    data object OnDestroy : MainActivityIntent()
}

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject ViewModel
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MusicAppContent()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.sendIntent(MainActivityIntent.RegisterNetworkCallback)
    }

    override fun onStop() {
        super.onStop()
        // Check if the activity is finishing to avoid unregistering on configuration changes
        if (!isChangingConfigurations) {
            viewModel.sendIntent(MainActivityIntent.UnregisterNetworkCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        viewModel.sendIntent(MainActivityIntent.OnDestroy)
    }
}

@Composable
fun MusicAppContent() {
    MusicAppTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val playerViewModel: IPlayerViewModel = hiltViewModel<PlayerViewModel>()
        val playerState = playerViewModel.state.collectAsState().value

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopAppBar(
                    currentRoute = currentRoute,
                    onNavigationIconClick = { navController.navigateUp() },
                    isPlayerExpanded = playerState.isExpanded,
                    onCollapseClick = {
                        playerViewModel.sendIntent(PlayerIntent.ToggleExpanded(isExpanded = false))
                    }
                )
            },
            bottomBar = {
                AppBottomNavigationBar(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }

                        playerViewModel.sendIntent(PlayerIntent.ToggleExpanded(isExpanded = false))
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                var heightPx by remember { mutableIntStateOf(0) }
                val density = LocalDensity.current
                val heightDp = with(density) { heightPx.toDp() }

                NavHost(
                    modifier = Modifier.padding(bottom = heightDp),
                    navController = navController,
                    startDestination = AppRoutes.TrendingRoute,
                ) {
                    composable<AppRoutes.TrendingRoute> {
                        val trendingViewModel: ITrendingScreenViewModel =
                            hiltViewModel<TrendingScreenViewModel>()
                        TrendingScreen(
                            viewModel = trendingViewModel,
                            onTrackClicked = {
                                playerViewModel.sendIntent(PlayerIntent.TrackSet(track = it))
                            }
                        )
                    }
                    composable<AppRoutes.SearchRoute> {
                        val searchViewModel: ISearchScreenViewModel =
                            hiltViewModel<SearchScreenViewModel>()
                        SearchScreen(
                            viewModel = searchViewModel,
                            onTrackClicked = {
                                playerViewModel.sendIntent(PlayerIntent.TrackSet(track = it))
                            }
                        )
                    }
                    composable<AppRoutes.PlaylistRoute> {
                        val playlistViewModel = hiltViewModel<PlaylistViewModel>()
                        PlaylistScreen(
                            viewModel = playlistViewModel,
                            onPlaylistClick = {
                                navController.navigate(AppRoutes.PlaylistDetailsRoute(it))
                            }
                        )
                    }

                    composable<AppRoutes.PlaylistDetailsRoute> { backStackEntry ->
                        PlaylistDetailsScreen()
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned { coords ->
                            heightPx = if (playerState.isExpanded) 0 else coords.size.height
                        }
                ) {
                    Player(viewModel = playerViewModel)
                }
            }
        }
    }
}
