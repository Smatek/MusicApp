package pl.skolimowski.musicapp.data.model

import androidx.room.Embedded
import androidx.room.Relation
import pl.skolimowski.musicapp.data.db.DatabaseConsts
import pl.skolimowski.musicapp.data.model.entity.TrackInfoEntity
import pl.skolimowski.musicapp.data.model.entity.TrackRecentlySearchedEntity
import pl.skolimowski.musicapp.data.model.entity.TrackStreamUrlEntity
import pl.skolimowski.musicapp.data.model.entity.TrackTrendingIndexEntity

interface Track {
    val trackInfo: TrackInfoEntity
    val streamUrl: TrackStreamUrlEntity?
}

interface TrendingTrack: Track {
    val trendingIndex: TrackTrendingIndexEntity?
}

interface RecentlySearchedTrack: Track {
    val searchTimestamp: TrackRecentlySearchedEntity?
}

open class TrackImpl(
    @Embedded override val trackInfo: TrackInfoEntity,
    @Relation(
        parentColumn = DatabaseConsts.TRACK_ID,
        entityColumn = DatabaseConsts.TRACK_STREAM_URL_TRACK_ID
    )
    override val streamUrl: TrackStreamUrlEntity? = null
): Track

class TrendingTrackImpl(
    trackInfo: TrackInfoEntity,
    streamUrl: TrackStreamUrlEntity? = null,
    @Relation(
        parentColumn = DatabaseConsts.TRACK_ID,
        entityColumn = DatabaseConsts.TRENDING_TRACK_TRACK_ID
    )
    override val trendingIndex: TrackTrendingIndexEntity? = null
): TrackImpl(trackInfo, streamUrl), TrendingTrack

class RecentlySearchedTrackImpl(
    trackInfo: TrackInfoEntity,
    streamUrl: TrackStreamUrlEntity? = null,
    @Relation(
        parentColumn = DatabaseConsts.TRACK_ID,
        entityColumn = DatabaseConsts.RECENTLY_SEARCHED_TRACK_ID
    )
    override val searchTimestamp: TrackRecentlySearchedEntity? = null
): TrackImpl(trackInfo, streamUrl), RecentlySearchedTrack