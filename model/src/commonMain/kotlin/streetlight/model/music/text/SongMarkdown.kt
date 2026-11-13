package streetlight.model.music.text

import kampfire.model.Ok
import kampfire.model.Outcome
import kampfire.model.Problem
import streetlight.model.music.ChordTrack
import streetlight.model.music.DrumTrack
import streetlight.model.music.LyricTrack
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.Song
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.TrackType
import streetlight.model.music.freeTrackNameOf

/**
 * The reader and writer of a whole song as markdown: its title as a heading with its artist on the next line, the text
 * of its music, then a heading per track naming it and its type, as `## Harmony (Melody)`, over its text.
 */
object SongMarkdown {
    /**
     * Reads [text] onto [song] and [music], or gives the problem of a track heading that names no known type.
     *
     * The notation of the music and each track is read as far as it can be, without its problems. Each track is given
     * a new id, and one named as an earlier one takes the name with the first free number after it, as `Melody 2`.
     */
    fun read(text: String, song: Song, music: Music, style: NotationStyle): Outcome<MusicSheet> {
        val lines = text.lines()
        val titleIndex = lines.indexOfFirst { it.startsWith(TITLE_PREFIX) }
        val trackIndexes = lines.indices.filter { lines[it].startsWith(TRACK_PREFIX) }

        // read title, artist and music
        val musicStart = titleIndex + 1
        val musicEnd = trackIndexes.firstOrNull() ?: lines.size
        val artist = lines.getOrNull(musicStart)?.takeIf { titleIndex >= 0 && it.isNotBlank() && ':' !in it }?.trim()
        val musicFrom = if (artist != null) musicStart + 1 else musicStart
        val readMusic = MusicParser.read(lines.subList(musicFrom, musicEnd).joinToString("\n"), music).value
        val readSong = song.copy(
            title = lines.getOrNull(titleIndex)?.removePrefix(TITLE_PREFIX)?.trim() ?: song.title,
            artist = artist ?: song.artist,
        )

        // read each track
        val names = SectionNames(readMusic.sections)
        val trackNames = mutableSetOf<String>()
        val tracks = trackIndexes.mapIndexed { index, headingIndex ->
            val heading = lines[headingIndex]
            val match = TRACK_HEADING.matchEntire(heading.trim())
            val type = match?.let { TrackType.entries.firstOrNull { it.name.equals(match.groupValues[2], ignoreCase = true) } }
                ?: return SongMarkdownProblem.trackHeadingOf(headingIndex + 1, heading)
            val trackId = TrackId.random()
            val trackName = freeTrackNameOf(match.groupValues[1].trim(), trackNames).also { trackNames.add(it) }
            val end = trackIndexes.getOrNull(index + 1) ?: lines.size
            val trackText = lines.subList(headingIndex + 1, end).joinToString("\n")
            when (type) {
                TrackType.Chords -> ChordParser(readMusic.key, style).read(trackId, readMusic.musicId, trackName, trackText, readMusic.time, names)
                TrackType.Melody -> MelodyParser(readMusic.key).read(trackId, readMusic.musicId, trackName, trackText, readMusic.time, names)
                TrackType.Lyrics -> LyricParser.read(trackId, readMusic.musicId, trackName, trackText, readMusic.time, names)
                TrackType.Drums -> DrumParser.read(trackId, readMusic.musicId, trackName, trackText, readMusic.time, names)
            }.value
        }

        return Ok(MusicSheet(readSong, readMusic.copy(sections = names.sections), tracks))
    }

    /** Writes [song] with its [music] and [tracks], chords spelled in [style]. */
    fun write(song: Song, music: Music, tracks: List<Track>, style: NotationStyle): String {
        val heading = listOfNotNull("$TITLE_PREFIX${song.title}", song.artist.takeIf { it.isNotBlank() }).joinToString("\n")
        val blocks = listOf(heading, MusicParser.write(music)) + tracks.flatMap { track ->
            listOf("$TRACK_PREFIX${track.name} (${track.trackType.name})", writeTrack(track, music, style))
        }
        return blocks.joinToString("\n\n", postfix = "\n")
    }

    private fun writeTrack(track: Track, music: Music, style: NotationStyle) = SectionNames(music.sections).let { names ->
        when (track) {
            is ChordTrack -> ChordParser(music.key, style).write(track, music.time, names)
            is MelodyTrack -> MelodyParser(music.key).write(track, music.time, names)
            is LyricTrack -> LyricParser.write(track, music.time, names)
            is DrumTrack -> DrumParser.write(track, music.time, names)
        }
    }
}

/** Problems of reading a song from markdown. */
object SongMarkdownProblem {
    /** The problem of the track [heading] on [line], which names no known type. */
    fun trackHeadingOf(line: Int, heading: String) = Problem("Line $line: $heading needs a track type, as ## Melody (Melody)")
}

private const val TITLE_PREFIX = "# "
private const val TRACK_PREFIX = "## "
private val TRACK_HEADING = Regex("""## (.+)\((\w+)\)""")
