package pl.skolimowski.musicapp.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.skolimowski.musicapp.BuildConfig
import pl.skolimowski.musicapp.network.AudiusApiService
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcher
import pl.skolimowski.musicapp.network.watcher.NetworkStateWatcherImpl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindingModule {

    @Binds
    @Singleton
    abstract fun bindNetworkStateWatcher(
        networkStateWatcherImpl: NetworkStateWatcherImpl
    ): NetworkStateWatcher
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkProviderModule {

    private const val AUDIUS_BASE_URL = "https://discoveryprovider.audius.co/v1/"

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        return loggingInterceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            // Add other interceptors (e.g., for auth) here if needed
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(AUDIUS_BASE_URL)
        .client(okHttpClient) // Use the custom OkHttpClient
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    @Provides
    @Singleton
    fun provideAudiusApiService(retrofit: Retrofit): AudiusApiService =
        retrofit.create(AudiusApiService::class.java)
} 