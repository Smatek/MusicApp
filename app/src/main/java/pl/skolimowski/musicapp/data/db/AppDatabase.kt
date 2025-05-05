package pl.skolimowski.musicapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import pl.skolimowski.musicapp.data.db.dao.PlaylistDao
import pl.skolimowski.musicapp.data.db.dao.TrackDao
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.TrackRecentlySearchedEntity
import pl.skolimowski.musicapp.data.model.entity.TrackStreamUrlEntity
import pl.skolimowski.musicapp.data.model.entity.TrackTrendingIndexEntity
import pl.skolimowski.musicapp.data.model.entity.PlaylistEntity
import pl.skolimowski.musicapp.data.model.entity.PlaylistTrackCrossRef

@Database(
    entities = [
        TrackInfoEntity::class,
        TrackTrendingIndexEntity::class,
        TrackStreamUrlEntity::class,
        TrackRecentlySearchedEntity::class,
        PlaylistEntity::class,
        PlaylistTrackCrossRef::class
    ],
    version = DatabaseConsts.DATABASE_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playListDao(): PlaylistDao
}