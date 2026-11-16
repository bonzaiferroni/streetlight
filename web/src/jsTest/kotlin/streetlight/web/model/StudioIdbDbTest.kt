package streetlight.web.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import kotlinx.coroutines.withTimeout
import streetlight.model.music.ChordTrack
import streetlight.model.music.Chroma
import streetlight.model.music.Clip
import streetlight.model.music.ClipId
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.MusicSheet
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.Study
import streetlight.model.music.StudyId
import streetlight.model.music.TimeSignature
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackClip
import streetlight.model.studio.StudioDb
import kotlin.js.Promise
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid

/** The contract of [StudioDb], held by [StudioIdbDb]. */
class StudioIdbDbTest {

    @Test
    fun `removing a song removes its musics, tracks, track clips and studies`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()

        db.song.removeSong(records.song.songId)

        assertEquals(emptyList(), db.music.flowMusics(records.song.songId).first())
        assertNull(db.track.flowTrack(records.track.trackId).first())
        assertEquals(emptyList(), db.trackClip.flowTrackClips(records.music.musicId).first())
        assertEquals(emptyList(), db.study.flowStudies().first())
        assertNotNull(db.clip.flowClip(records.clip.clipId).first(), "a clip outlives the songs it is bound in")
    }

    @Test
    fun `removing a clip removes its track clips and leaves its track`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()

        db.clip.removeClip(records.clip.clipId)

        assertEquals(emptyList(), db.trackClip.flowTrackClips(records.music.musicId).first())
        assertNotNull(db.track.flowTrack(records.track.trackId).first())
    }

    @Test
    fun `a music's tracks emit again when one is saved, and not when another music's track is saved`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()
        val otherMusic = musicOf(records.song.songId)
        val secondTrack = trackOf(records.music.musicId)
        val emissions = mutableListOf<List<Track>>()

        val collection = launch { db.track.flowTracks(records.music.musicId).collect { emissions += it } }
        awaitCount(emissions, 1)
        db.track.saveTrack(trackOf(otherMusic.musicId))
        delay(EMISSION_WAIT)
        db.track.saveTrack(secondTrack)
        awaitCount(emissions, 2)
        collection.cancel()

        assertEquals(2, emissions.size)
        assertEquals(listOf(records.track), emissions[0])
        assertEquals(setOf(records.track, secondTrack), emissions[1].toSet())
    }

    @Test
    fun `a saved music sheet reads back whole from a newly opened database`() = runStudioTest {
        val name = testDbName()
        val music = musicOf(SongId.random())
        val sheet = MusicSheet(music, listOf(trackOf(music.musicId)))

        StudioIdbDb(name).music.saveMusicSheet(sheet)

        assertEquals(sheet, StudioIdbDb(name).music.readMusicSheet(music.musicId))
    }
}

/** One record of each kind, bound together. */
private data class StudioRecords(
    val song: Song,
    val music: Music,
    val track: Track,
    val clip: Clip,
    val trackClip: TrackClip,
)

private suspend fun StudioDb.saveRecords(): StudioRecords {
    val song = Song(SongId.random(), "Title", "Artist")
    val music = musicOf(song.songId)
    val track = trackOf(music.musicId)
    val clip = Clip(ClipId.random(), "Take", music.bpm, music.time, music.key, bars = 4, leadSeconds = 0.0)
    val trackClip = TrackClip(music.musicId, track.trackId, null, clip.clipId)

    this.song.saveSong(song)
    this.music.saveMusic(music)
    this.track.saveTrack(track)
    this.clip.saveClip(clip)
    this.trackClip.saveTrackClip(trackClip)
    this.study.saveStudy(Study(StudyId(Uuid.random()), track.trackId, null))
    return StudioRecords(song, music, track, clip, trackClip)
}

private fun musicOf(songId: SongId) =
    Music(MusicId.random(), songId, "Name", Key(Chroma.of(0), Mode.Major), TimeSignature(4, 4))

private fun trackOf(musicId: MusicId) = ChordTrack(TrackId.random(), musicId, "Chords", emptyList(), emptyList(), emptyMap())

private fun testDbName() = "studio-test-${Uuid.random()}"

private suspend fun awaitCount(emissions: List<*>, count: Int) {
    while (emissions.size < count) delay(EMISSION_WAIT)
}

private fun runStudioTest(block: suspend CoroutineScope.() -> Unit): Promise<Unit> = MainScope().promise {
    withTimeout(TEST_TIMEOUT) { coroutineScope { block() } }
}

private const val EMISSION_WAIT = 20L
private const val TEST_TIMEOUT = 5_000L
