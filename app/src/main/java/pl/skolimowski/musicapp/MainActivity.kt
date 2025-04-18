package pl.skolimowski.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import pl.skolimowski.musicapp.ui.theme.MusicAppTheme

@Serializable
object Trending

@Serializable
object Search

@Serializable
data class Details(val itemId: String) // Example: Details might need an ID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MusicAppTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Trending, // Start with TrendingScreen
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<Trending> {
                            TrendingScreen()
                        }
                        composable<Search> {
                            SearchScreen()
                        }
                        composable<Details> { backStackEntry ->
                            val detailsArgs = backStackEntry.toRoute<Details>()
                            DetailsScreen(detailsArgs.itemId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Trending Screen",
        modifier = modifier
    )
}

@Composable
fun SearchScreen(modifier: Modifier = Modifier) {
    Text(
        text = "Search Screen",
        modifier = modifier
    )
}

@Composable
fun DetailsScreen(itemId: String, modifier: Modifier = Modifier) {
    Text(
        text = "Details Screen for item: $itemId", // Displaying the ID for demonstration
        modifier = modifier
    )
}
