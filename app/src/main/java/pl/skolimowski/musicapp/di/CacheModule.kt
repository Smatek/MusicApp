package pl.skolimowski.musicapp.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.skolimowski.musicapp.data.cache.ITrackCacheManager
import pl.skolimowski.musicapp.data.cache.TrackCacheManager
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

// Qualifiers to distinguish between upstream and caching factories
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UpstreamDataSource

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CachingDataSource

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheBindingModule {
    @Binds
    @Singleton
    abstract fun bindCacheManager(trackCacheManager: TrackCacheManager): ITrackCacheManager
}

@Module
@InstallIn(SingletonComponent::class)
@UnstableApi
object CacheModule {

    private const val CACHE_SUBDIR = "media_cache"
    private const val CACHE_SIZE_BYTES = 100 * 1024 * 1024L // 100MB

    @Provides
    @Singleton
    fun provideCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, CACHE_SUBDIR)
        val databaseProvider = StandaloneDatabaseProvider(context)
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES)
        return SimpleCache(cacheDir, cacheEvictor, databaseProvider)
    }

    @Provides
    @Singleton
    @UpstreamDataSource
    fun provideUpstreamDataSourceFactory(): DataSource.Factory {
        return DefaultHttpDataSource.Factory()
    }

    @Provides
    @Singleton
    @CachingDataSource
    fun provideCacheDataSourceFactory(
        cache: Cache,
        @UpstreamDataSource upstreamDataSourceFactory: DataSource.Factory
    ): DataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }
} 