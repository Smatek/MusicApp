package pl.skolimowski.musicapp.data.cache

import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.CacheWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pl.skolimowski.musicapp.di.CachingDataSource
import pl.skolimowski.musicapp.di.DispatcherProvider
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

enum class CacheStatus {
    NOT_CACHED,
    CACHING,
    CACHED,
    ERROR
}

@OptIn(UnstableApi::class)
interface ITrackCacheManager {
    val cacheStatusFlow: StateFlow<Map<Uri, CacheStatus>>
    fun startPreCaching(uri: Uri, length: Long = TrackCacheManager.PRECACHE_LENGTH_BYTES)
}

@OptIn(UnstableApi::class)
@Singleton
class TrackCacheManager @Inject constructor(
    private val cache: Cache,
    @CachingDataSource private val cacheDataSourceFactory: DataSource.Factory,
    dispatchers: DispatcherProvider
) : ITrackCacheManager {

    private val _cacheStatusFlow = MutableStateFlow<Map<Uri, CacheStatus>>(emptyMap())
    override val cacheStatusFlow: StateFlow<Map<Uri, CacheStatus>> = _cacheStatusFlow.asStateFlow()

    private val preCachingJobs = ConcurrentHashMap<Uri, Job>()

    // Use a dedicated scope for caching operations to manage lifecycle
    private val cachingScope = CoroutineScope(SupervisorJob() + dispatchers.io)

    companion object {
        const val PRECACHE_LENGTH_BYTES = 512 * 1024L // Cache 512KB (adjust as needed)
        private const val TAG = "TrackCacheManager"
    }

    override fun startPreCaching(uri: Uri, length: Long) {
        val currentStatus = _cacheStatusFlow.value[uri]
        if (currentStatus == CacheStatus.CACHED || currentStatus == CacheStatus.CACHING) {
            Log.d(TAG, "Skipping pre-cache for $uri, status: $currentStatus")
            return
        }

        preCachingJobs[uri]?.cancel()

        val job = launchCachingJob(uri, length)
        preCachingJobs[uri] = job

        job.invokeOnCompletion { cause ->
            if (cause != null && cause !is CancellationException) {
                Log.w(TAG, "Caching job for $uri completed with error: ${cause.message}")
            }

            preCachingJobs.remove(uri)
        }
    }

    private fun launchCachingJob(uri: Uri, length: Long): Job {
        return cachingScope.launch {
            try {
                updateStatus(uri, CacheStatus.CACHING)
                Log.i(TAG, "Starting pre-cache for: $uri ($length bytes)")

                cache(uri, length)

                val isFullyCached = cache.isCached(uri.toString(), 0, length)

                if (isFullyCached) {
                    updateStatus(uri, CacheStatus.CACHED)
                    Log.i(TAG, "Successfully pre-cached requested $length bytes for: $uri")
                } else {
                    updateStatus(uri, CacheStatus.ERROR)
                    Log.w(TAG, "Pre-caching incomplete/failed for $uri")
                }

            } catch (e: Exception) {
                if (e is CancellationException) {
                    updateStatus(uri, CacheStatus.NOT_CACHED)
                    Log.i(TAG, "Pre-caching cancelled for: $uri")
                    throw e
                } else {
                    updateStatus(uri, CacheStatus.ERROR)
                    Log.e(TAG, "Error pre-caching $uri", e)
                }
            } finally {
                preCachingJobs.remove(uri)
            }
        }
    }

    private fun cache(uri: Uri, length: Long) {
        val dataSpec = DataSpec.Builder()
            .setUri(uri)
            .setPosition(0)
            .setLength(length)
            .build()

        val cacheDataSource = cacheDataSourceFactory.createDataSource() as CacheDataSource
        val cacheWriter = CacheWriter(cacheDataSource, dataSpec, null, null)
        cacheWriter.cache()
    }

    private fun updateStatus(uri: Uri, status: CacheStatus) {
        _cacheStatusFlow.update { currentMap ->
            currentMap + (uri to status)
        }
    }
}