package streetlight.web.model

import streetlight.model.music.Clip
import streetlight.model.music.Mix
import streetlight.model.music.MixId
import streetlight.model.music.Music
import streetlight.model.music.SectionClip
import streetlight.model.music.Song
import streetlight.model.music.Study
import streetlight.model.music.Track
import streetlight.model.music.TrackSectionId
import streetlight.model.studio.StudioDb
import streetlight.web.idb.IdbDb

/** The [StudioDb] kept in the browser database [name]. */
class StudioIdbDb(name: String = STUDIO_DB_NAME) : StudioDb {
    private val db = IdbDb(name, version = 1)

    private val songs = db.table("songs", Song.serializer()) { it.songId.toString() }
    private val musics = db.table("musics", Music.serializer()) { it.musicId.toString() }
        .references(songs) { it.songId.toString() }
    private val tracks = db.table("tracks", Track.serializer()) { it.trackId.toString() }
        .references(musics) { it.musicId.toString() }
    private val mixes = db.table("mixes", Mix.serializer()) { it.mixId.toString() }
        .references(musics) { it.musicId.toString() }
    private val clips = db.table("clips", Clip.serializer()) { it.clipId.toString() }
    private val sectionClips = db.table("sectionClips", SectionClip.serializer()) { keyOf(it.mixId, it.trackSectionId) }
        .references(mixes) { it.mixId.toString() }
        .references(tracks) { it.trackSectionId.trackId.toString() }
        .references(clips) { it.clipId.toString() }
    private val studies = db.table("studies", Study.serializer()) { it.studyId.toString() }
        .references(tracks) { it.trackId.toString() }

    private val songDao = SongIdbDao(songs)
    private val musicDao = MusicIdbDao(db, songs, musics, tracks)
    private val trackDao = TrackIdbDao(tracks)
    private val mixDao = MixIdbDao(mixes)
    private val sectionClipDao = SectionClipIdbDao(sectionClips)
    private val clipDao = ClipIdbDao(clips)
    private val studyDao = StudyIdbDao(studies, tracks)

    override fun songDao() = songDao
    override fun musicDao() = musicDao
    override fun trackDao() = trackDao
    override fun mixDao() = mixDao
    override fun sectionClipDao() = sectionClipDao
    override fun clipDao() = clipDao
    override fun studyDao() = studyDao
}

/** The key of the clip bound to the track's section [trackSectionId] in the mix [mixId]. */
internal fun keyOf(mixId: MixId, trackSectionId: TrackSectionId) =
    "$mixId/${trackSectionId.trackId}/${trackSectionId.sectionId}"

private const val STUDIO_DB_NAME = "streetlight-studio"
