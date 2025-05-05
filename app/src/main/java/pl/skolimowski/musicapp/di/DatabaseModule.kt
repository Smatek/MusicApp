package pl.skolimowski.musicapp.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.skolimowski.musicapp.data.db.AppDatabase
import pl.skolimowski.musicapp.data.db.DatabaseConsts
import pl.skolimowski.musicapp.data.db.dao.PlaylistDao
import pl.skolimowski.musicapp.data.db.dao.TrackDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            DatabaseConsts.DATABASE_NAME
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(appDatabase: AppDatabase): TrackDao {
        return appDatabase.trackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(appDatabase: AppDatabase): PlaylistDao {
        return appDatabase.playListDao()
    }
}