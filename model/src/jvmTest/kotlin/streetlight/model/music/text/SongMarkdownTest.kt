package streetlight.model.music.text

import kampfire.model.Ok
import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.MusicId
import streetlight.model.music.Song
import streetlight.model.music.SongId
import streetlight.model.music.TimeSignature
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SongMarkdownTest {

    @Test
    fun `amazing grace is written back as it was read`() = assertWrittenAsRead("amazing-grace.md")

    @Test
    fun `row your boat is written back as it was read`() = assertWrittenAsRead("row-your-boat.md")

    @Test
    fun `each track plays a notation for every section of the arrangement`() {
        val read = readSong("amazing-grace.md")
        val arrangement = read.sheet.music.arrangement

        assertEquals(listOf("Verse A", "Verse B", "Verse C"), arrangement.map { read.sheet.music.nameOf(it) })
        assertEquals(listOf("Chords", "Melody", "Harmony", "Drums", "Lyrics"), read.sheet.tracks.map { it.name })
        read.sheet.tracks.forEach { track ->
            assertEquals(arrangement.toSet(), track.sections.keys, "${track.name} should play every section")
        }
    }

    private fun assertWrittenAsRead(fileName: String) {
        val text = File(SONGS_PATH, fileName).readText()
        val read = readSong(fileName)

        val written = SongMarkdown.write(read.song, read.sheet.music, read.sheet.tracks)

        assertEquals(text.trimEnd(), written.trimEnd())
    }

    private fun readSong(fileName: String): SongMarkdownRead {
        val songId = SongId.random()
        val song = Song(songId, "", "")
        val music = Music(MusicId.random(), songId, "", Key(Chroma.of(0), Mode.Major), TimeSignature(4, 4))
        val outcome = SongMarkdown.read(File(SONGS_PATH, fileName).readText(), song, music)
        return assertIs<Ok<SongMarkdownRead>>(outcome).data
    }
}

private const val SONGS_PATH = "../docs/songs"
