package pl.skolimowski.musicapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.skolimowski.musicapp.player.MusicController
import pl.skolimowski.musicapp.player.MusicControllerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Application scope
abstract class MusicControllerModule {

    @Binds
    @Singleton // Ensure only one instance of MusicControllerImpl exists
    abstract fun bindMusicController(
        musicControllerImpl: MusicControllerImpl
    ): MusicController

}