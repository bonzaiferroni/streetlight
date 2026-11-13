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
    fun `tracks that share a section name share its id and keep their own ids`() {
        val read = readSong("amazing-grace.md")
        val song = read.sheet.music.sectionOf("Song")?.sectionId

        assertEquals(5, read.sheet.tracks.map { it.trackId }.toSet().size, "each track should have its own id")
        assertEquals(listOf("Chords", "Melody", "Harmony", "Drums", "Lyrics"), read.sheet.tracks.map { it.name })
        read.sheet.tracks.forEach { track ->
            val ids = track.sections.map { it.sectionId } + track.arrangements.map { it.sectionId }
            assertEquals(true, song in ids, "${track.name} should define Song")
        }
    }

    private fun assertWrittenAsRead(fileName: String) {
        val text = File(SONGS_PATH, fileName).readText()
        val read = readSong(fileName)

        val written = SongMarkdown.write(read.song, read.sheet.music, read.sheet.tracks, NotationStyle.Letters)

        assertEquals(text.trimEnd(), written.trimEnd())
    }

    private fun readSong(fileName: String): SongMarkdownRead {
        val songId = SongId.random()
        val song = Song(songId, "", "")
        val music = Music(MusicId.random(), songId, "", Key(Chroma.of(0), Mode.Major), TimeSignature(4, 4))
        val outcome = SongMarkdown.read(File(SONGS_PATH, fileName).readText(), song, music, NotationStyle.Letters)
        return assertIs<Ok<SongMarkdownRead>>(outcome).data
    }
}

private const val SONGS_PATH = "../docs/songs"
