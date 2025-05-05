package pl.skolimowski.musicapp.ui.playlist

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.skolimowski.musicapp.data.model.Playlist
import pl.skolimowski.musicapp.data.model.Track
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.repository.PlaylistRepository
import pl.skolimowski.musicapp.data.repository.Result
import java.io.IOException

data class TestPlaylist(
    val testId: Long,
    val name: String,
    val artworkUrl: String?
) : Playlist {
    override val playlistEntity: PlaylistEntity = PlaylistEntity(
        id = testId.toString(),
        name = name,
        artworkUrl = artworkUrl,
        description = null
    )
    override val tracks: List<Track> = emptyList()
}

@ExperimentalCoroutinesApi
class PlaylistViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var playlistRepositoryMock: PlaylistRepository
    private lateinit var viewModel: PlaylistViewModel

    private val samplePlaylists: List<Playlist> = listOf(
        TestPlaylist(testId = 1, name = "Trending 1", artworkUrl = "url1"),
        TestPlaylist(testId = 2, name = "Trending 2", artworkUrl = "url2")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        playlistRepositoryMock = mockk(relaxed = true)
        every { playlistRepositoryMock.observePlaylists() } returns flowOf<List<Playlist>>(samplePlaylists)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = PlaylistViewModel(playlistRepositoryMock)
    }

    @Test
    fun `given repository success when viewmodel init then state contains loading true then false and playlist data`() = runTest(testDispatcher) {
        coEvery { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() } returns Result.Success(Unit)
        every { playlistRepositoryMock.observePlaylists() } returns flowOf<List<Playlist>>(emptyList(), samplePlaylists)

        createViewModel()

        viewModel.state.test {
            assertEquals(PlaylistState(), awaitItem())
            assertEquals(PlaylistState(isLoading = true), awaitItem())
            assertEquals(PlaylistState(isLoading = false, trendingPlaylists = emptyList()), awaitItem())
            assertEquals(PlaylistState(isLoading = false, trendingPlaylists = samplePlaylists), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() }
        coVerify(exactly = 1) { playlistRepositoryMock.observePlaylists() }
    }

    @Test
    fun `given repository fetch error when viewmodel init then state contains loading true then false and error`() = runTest(testDispatcher) {
        val errorMessage = "Network error"
        val exception = IOException(errorMessage)
        coEvery { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() } returns Result.Error(exception)
        every { playlistRepositoryMock.observePlaylists() } returns flowOf<List<Playlist>>(emptyList())

        createViewModel()

        viewModel.state.test {
            assertEquals(PlaylistState(), awaitItem())
            assertEquals(PlaylistState(isLoading = true), awaitItem())
            val expectedErrorState = PlaylistState(
                isLoading = false,
                trendingPlaylists = emptyList(),
                error = "Failed to fetch playlists: $errorMessage"
            )
            assertEquals(expectedErrorState, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() }
        coVerify(exactly = 1) { playlistRepositoryMock.observePlaylists() }
    }

    @Test
    fun `given repository observe error when viewmodel init then state contains error`() = runTest(testDispatcher) {
        val errorMessage = "Database error"
        val exception = RuntimeException(errorMessage)
        coEvery { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() } returns Result.Success(Unit)
        every { playlistRepositoryMock.observePlaylists() } returns flow<List<Playlist>> { throw exception }

        createViewModel()

        viewModel.state.test {
            assertEquals(PlaylistState(), awaitItem())
            assertEquals(PlaylistState(isLoading = true), awaitItem())
            assertEquals(PlaylistState(isLoading = false), awaitItem())
            val expectedErrorState = PlaylistState(
                isLoading = false,
                error = "Failed to observe playlists: $errorMessage"
            )
            assertEquals(expectedErrorState, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 1) { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() }
        coVerify(exactly = 1) { playlistRepositoryMock.observePlaylists() }
    }


    @Test
    fun `when Refresh intent received then fetchPlaylists is called again`() = runTest(testDispatcher) {
        coEvery { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() } returns Result.Success(Unit)
        every { playlistRepositoryMock.observePlaylists() } returns flowOf<List<Playlist>>(samplePlaylists)

        createViewModel()

        viewModel.state.test {
            skipItems(3)
            assertEquals(PlaylistState(isLoading = false, trendingPlaylists = samplePlaylists), awaitItem())
        }

        coVerify(exactly = 1) { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() }

        viewModel.handleIntent(PlaylistIntent.Refresh)

        viewModel.state.test {
            assertEquals(PlaylistState(isLoading = false, trendingPlaylists = samplePlaylists), awaitItem())
            assertEquals(PlaylistState(isLoading = true, trendingPlaylists = samplePlaylists), awaitItem())
            assertEquals(PlaylistState(isLoading = false, trendingPlaylists = samplePlaylists), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 2) { playlistRepositoryMock.fetchAndSaveTrendingPlaylists() }
    }
} 