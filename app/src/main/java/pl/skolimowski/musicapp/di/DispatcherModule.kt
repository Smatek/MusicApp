package pl.skolimowski.musicapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DispatcherProviderImpl()
}

// Implementation of Dispatcher Provider
class DispatcherProviderImpl : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val default: CoroutineDispatcher = Dispatchers.Default
}
