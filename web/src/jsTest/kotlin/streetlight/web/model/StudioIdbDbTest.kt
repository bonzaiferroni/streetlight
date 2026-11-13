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
import streetlight.model.music.SectionClip
import streetlight.model.music.SectionId
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.Study
import streetlight.model.music.StudyId
import streetlight.model.music.TimeSignature
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackSectionId
import streetlight.model.music.mixOf
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
    fun `removing a song removes its musics, tracks, mixes, section clips and studies`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()

        db.songDao().removeSong(records.song.songId)

        assertEquals(emptyList(), db.musicDao().flowMusics(records.song.songId).first())
        assertNull(db.trackDao().flowTrack(records.track.trackId).first())
        assertEquals(emptyList(), db.mixDao().flowMixes(records.music.musicId).first())
        assertEquals(emptyList(), db.sectionClipDao().flowSectionClips(records.sectionClip.mixId).first())
        assertEquals(emptyList(), db.studyDao().flowStudies().first())
        assertNotNull(db.clipDao().flowClip(records.clip.clipId).first(), "a clip outlives the songs it is bound in")
    }

    @Test
    fun `removing a clip removes its section clips and leaves its mix`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()

        db.clipDao().removeClip(records.clip.clipId)

        assertEquals(emptyList(), db.sectionClipDao().flowSectionClips(records.sectionClip.mixId).first())
        assertNotNull(db.mixDao().flowMix(records.sectionClip.mixId).first())
    }

    @Test
    fun `a music's tracks emit again when one is saved, and not when another music's track is saved`() = runStudioTest {
        val db = StudioIdbDb(testDbName())
        val records = db.saveRecords()
        val otherMusic = musicOf(records.song.songId)
        val secondTrack = trackOf(records.music.musicId)
        val emissions = mutableListOf<List<Track>>()

        val collection = launch { db.trackDao().flowTracks(records.music.musicId).collect { emissions += it } }
        awaitCount(emissions, 1)
        db.trackDao().saveTrack(trackOf(otherMusic.musicId))
        delay(EMISSION_WAIT)
        db.trackDao().saveTrack(secondTrack)
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

        StudioIdbDb(name).musicDao().saveMusicSheet(sheet)

        assertEquals(sheet, StudioIdbDb(name).musicDao().readMusicSheet(music.musicId))
    }
}

/** One record of each kind, bound together. */
private data class StudioRecords(
    val song: Song,
    val music: Music,
    val track: Track,
    val clip: Clip,
    val sectionClip: SectionClip,
)

private suspend fun StudioDb.saveRecords(): StudioRecords {
    val song = Song(SongId.random(), "Title", "Artist")
    val music = musicOf(song.songId)
    val track = trackOf(music.musicId)
    val mix = mixOf(music, "Mix")
    val clip = Clip(ClipId.random(), "Take", music.bpm, music.time, music.key, bars = 4, leadSeconds = 0.0)
    val sectionClip = SectionClip(mix.mixId, TrackSectionId(track.trackId, SectionId.random()), clip.clipId)

    songDao().saveSong(song)
    musicDao().saveMusic(music)
    trackDao().saveTrack(track)
    mixDao().saveMix(mix)
    clipDao().saveClip(clip)
    sectionClipDao().saveSectionClip(sectionClip)
    studyDao().saveStudy(Study(StudyId(Uuid.random()), track.trackId, null))
    return StudioRecords(song, music, track, clip, sectionClip)
}

private fun musicOf(songId: SongId) =
    Music(MusicId.random(), songId, "Name", Key(Chroma.of(0), Mode.Major), TimeSignature(4, 4))

private fun trackOf(musicId: MusicId) = ChordTrack(TrackId.random(), musicId, "Chords", emptyList(), emptyList())

private fun testDbName() = "studio-test-${Uuid.random()}"

private suspend fun awaitCount(emissions: List<*>, count: Int) {
    while (emissions.size < count) delay(EMISSION_WAIT)
}

private fun runStudioTest(block: suspend CoroutineScope.() -> Unit): Promise<Unit> = MainScope().promise {
    withTimeout(TEST_TIMEOUT) { coroutineScope { block() } }
}

private const val EMISSION_WAIT = 20L
private const val TEST_TIMEOUT = 5_000L
