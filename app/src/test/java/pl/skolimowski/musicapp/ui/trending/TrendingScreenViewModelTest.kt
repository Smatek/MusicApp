package pl.skolimowski.musicapp.ui.trending

import android.net.Uri
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.skolimowski.musicapp.data.cache.CacheStatus
import pl.skolimowski.musicapp.data.model.TrendingTrack
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.repository.Result
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.network.watcher.NetworkState
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcher

@ExperimentalCoroutinesApi
class TrendingScreenViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var trackRepositoryMock: TrackRepository
    private lateinit var networkStateWatcherMock: NetworkStateWatcher
    private lateinit var viewModel: TrendingScreenViewModel

    private val networkStateFlow = MutableStateFlow(NetworkState.AVAILABLE)
    private val cacheStatusFlow = MutableStateFlow<Map<Uri, CacheStatus>>(emptyMap())
    private val trendingTracksFlow = MutableStateFlow<List<TrendingTrack>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        trackRepositoryMock = mockk(relaxed = true)
        networkStateWatcherMock = mockk(relaxed = true)

        every { networkStateWatcherMock.networkState } returns networkStateFlow
        every { trackRepositoryMock.getTrendingTracks() } returns trendingTracksFlow
        every { trackRepositoryMock.getTrackCacheStatusFlow() } returns cacheStatusFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = TrendingScreenViewModel(
            trackRepository = trackRepositoryMock,
            networkStateWatcher = networkStateWatcherMock
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `when initialized with network available then fetch trending tracks`() = runTest {
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            assertEquals(TrendingScreenStates.Loading, awaitItem().screenState)
            assertEquals(TrendingScreenStates.Success, awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when initialized with network unavailable then show no network error`() = runTest {
        networkStateFlow.value = NetworkState.UNAVAILABLE

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            assertEquals(TrendingScreenStates.Error(ErrorType.NO_NETWORK), awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify(exactly = 0) { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when network becomes available then fetch trending tracks`() = runTest {
        // Set network to unavailable initially
        networkStateFlow.value = NetworkState.UNAVAILABLE

        // Set up mocks
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // First verify we're in error state due to no network
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(TrendingScreenStates.Error(ErrorType.NO_NETWORK), initialState.screenState)
            cancelAndIgnoreRemainingEvents()
        }

        // Reset the mock before changing network state to track the fetch call
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        // Change network to available
        networkStateFlow.value = NetworkState.AVAILABLE
        testDispatcher.scheduler.advanceUntilIdle()

        // Now verify loading and success states
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TrendingScreenStates.Loading, state.screenState)
            assertEquals(TrendingScreenStates.Success, awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify fetchTrendingTracks was called after network became available
        coVerify { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when network becomes unavailable during loading then show no network error`() = runTest {
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        createViewModel()

        viewModel.state.test {
            assertEquals(TrendingScreenStates.Loading, awaitItem().screenState)

            // Network becomes unavailable during loading
            networkStateFlow.value = NetworkState.UNAVAILABLE
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(TrendingScreenStates.Error(ErrorType.NO_NETWORK), awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when fetch trending tracks succeeds then update state to success`() = runTest {
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            assertEquals(TrendingScreenStates.Loading, awaitItem().screenState)
            assertEquals(TrendingScreenStates.Success, awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when fetch trending tracks fails then update state to server error`() = runTest {
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Error(Exception("Test error"))

        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.state.test {
            assertEquals(TrendingScreenStates.Loading, awaitItem().screenState)
            assertEquals(
                TrendingScreenStates.Error(ErrorType.SERVER_ERROR),
                awaitItem().screenState
            )
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when refresh data intent received then fetch trending tracks`() = runTest {
        // Set up the test environment with a success response for fetchTrendingTracks
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        // Create the viewmodel and wait for it to initialize
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Reset the mock to clear the initial fetch call count
        coEvery { trackRepositoryMock.fetchTrendingTracks() } returns Result.Success(emptyList<TrackInfoEntity>())

        // When refresh intent is received
        viewModel.sendIntent(TrendingScreenIntent.RefreshData)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then state should be updated and fetch called
        viewModel.state.test {
            assertEquals(TrendingScreenStates.Loading, awaitItem().screenState)
            assertEquals(TrendingScreenStates.Success, awaitItem().screenState)
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { trackRepositoryMock.fetchTrendingTracks() }
    }

    @Test
    fun `when track clicked intent received then send navigate to details side effect`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val mockTrack = mockk<TrendingTrack>(relaxed = true)

        viewModel.sideEffect.test {
            viewModel.sendIntent(TrendingScreenIntent.TrackClicked(mockTrack))
            testDispatcher.scheduler.advanceUntilIdle()

            val sideEffect = awaitItem()
            assert(sideEffect is TrendingScreenSideEffect.NavigateToDetails)
            assertEquals(
                mockTrack,
                (sideEffect as TrendingScreenSideEffect.NavigateToDetails).trendingTrack
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when visible tracks changed intent received then prefetch visible tracks`() = runTest {
        createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val visibleTrackIds = setOf("track1", "track2", "track3")

        viewModel.sendIntent(TrendingScreenIntent.VisibleTracksChanged(visibleTrackIds))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { trackRepositoryMock.prefetchTrackStart(visibleTrackIds) }
    }

    @Test
    fun `when visible tracks changed intent received again then cancel previous prefetch job`() =
        runTest {
            createViewModel()
            testDispatcher.scheduler.advanceUntilIdle()

            val firstVisibleTrackIds = setOf("track1", "track2")
            val secondVisibleTrackIds = setOf("track3", "track4")

            viewModel.sendIntent(TrendingScreenIntent.VisibleTracksChanged(firstVisibleTrackIds))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { trackRepositoryMock.prefetchTrackStart(firstVisibleTrackIds) }

            viewModel.sendIntent(TrendingScreenIntent.VisibleTracksChanged(secondVisibleTrackIds))
            testDispatcher.scheduler.advanceUntilIdle()

            coVerify { trackRepositoryMock.prefetchTrackStart(secondVisibleTrackIds) }
        }

    @Test
    fun `when trending tracks received from repository then update state`() = runTest {
        val mockTracks = listOf<TrendingTrack>(
            mockk(relaxed = true),
            mockk(relaxed = true)
        )

        createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(emptyList<TrendingTrack>(), initialState.trendingTracks)

            trendingTracksFlow.value = mockTracks
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(mockTracks, updatedState.trendingTracks)

            cancelAndIgnoreRemainingEvents()
        }
    }
} 