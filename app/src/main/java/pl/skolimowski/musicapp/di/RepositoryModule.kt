package pl.skolimowski.musicapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.skolimowski.musicapp.data.repository.PlaylistRepository
import pl.skolimowski.musicapp.data.repository.PlaylistRepositoryImpl
import pl.skolimowski.musicapp.data.repository.TrackRepository
import pl.skolimowski.musicapp.data.repository.TrackRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTrackRepository(
        trackRepositoryImpl: TrackRepositoryImpl
    ): TrackRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(playlistRepositoryImpl: PlaylistRepositoryImpl): PlaylistRepository
} 