package streetlight.web.model

import streetlight.model.music.Clip
import streetlight.model.music.Music
import streetlight.model.music.Song
import streetlight.model.music.Study
import streetlight.model.music.Track
import streetlight.model.music.TrackClip
import streetlight.model.music.NotationId
import streetlight.model.music.TrackId
import streetlight.model.studio.StudioDb
import streetlight.web.idb.IdbDb

/** The [StudioDb] kept in the browser database [name]. */
class StudioIdbDb(name: String = STUDIO_DB_NAME) : StudioDb {
    private val db = IdbDb(name, version = 2)

    private val songs = db.table("songs", Song.serializer()) { it.songId.toString() }
    private val musics = db.table("musics", Music.serializer()) { it.musicId.toString() }
        .references(songs) { it.songId.toString() }
    private val tracks = db.table("tracks", Track.serializer()) { it.trackId.toString() }
        .references(musics) { it.musicId.toString() }
    private val clips = db.table("clips", Clip.serializer()) { it.clipId.toString() }
    private val trackClips = db.table("trackClips", TrackClip.serializer()) { keyOf(it.trackId, it.notationId) }
        .references(tracks) { it.trackId.toString() }
        .references(clips) { it.clipId.toString() }
    private val studies = db.table("studies", Study.serializer()) { keyOf(it.trackId, it.notationId) }
        .references(tracks) { it.trackId.toString() }

    override val song = SongIdbDao(songs)
    override val music = MusicIdbDao(db, songs, musics, tracks)
    override val track = TrackIdbDao(tracks)
    override val trackClip = TrackClipIdbDao(trackClips)
    override val clip = ClipIdbDao(clips)
    override val study = StudyIdbDao(studies, musics, tracks)
}

/** The key of the clip bound to the notation [notationId] of the track [trackId], or the whole track when `null`. */
internal fun keyOf(trackId: TrackId, notationId: NotationId?) = "$trackId/${notationId ?: WHOLE_TRACK_KEY}"

private const val STUDIO_DB_NAME = "streetlight-studio"
private const val WHOLE_TRACK_KEY = "track"
