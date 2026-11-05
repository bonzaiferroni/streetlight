package streetlight.model.data

/**
 * The parser of a song's music written as labeled lines, as `Key: C Major`, `Time: 3/4`, `Tempo: 90` and
 * `Arrangement: Intro Verse Chorus`.
 *
 * A label is read in any case. A line left out keeps the value it had, and a line that cannot be read is reported.
 */
object MusicParser {
    /** Reads [text] onto [music]. */
    fun read(text: String, music: Music): NotationRead<Music> {
        val errors = mutableListOf<NotationError>()
        var result = music
        text.lines().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed
            fun error(message: String) = errors.add(NotationError(index + 1, message))
            val label = line.substringBefore(':', "").trim()
            val value = line.substringAfter(':').trim()
            when {
                label.equals(KEY, ignoreCase = true) -> readKey(value)?.let { result = result.copy(key = it) }
                    ?: error("Unknown key $value")
                label.equals(TIME, ignoreCase = true) -> readTime(value)?.let { result = result.copy(time = it) }
                    ?: error("Unknown time $value")
                label.equals(TEMPO, ignoreCase = true) -> value.toIntOrNull()?.takeIf { it > 0 }
                    ?.let { result = result.copy(bpm = it) }
                    ?: error("Unknown tempo $value")
                label.equals(ARRANGEMENT, ignoreCase = true) ->
                    result = result.copy(arrangement = tokensOf(value).map { SectionId(it) })
                else -> error("Unknown line $line")
            }
        }
        return NotationRead(result, errors)
    }

    /** Writes [music] as the text [read] reads. */
    fun write(music: Music) = buildString {
        appendLine("$KEY: ${Chromatic.ofPitch(music.key.tonic.value).label} ${music.key.mode.name}")
        appendLine("$TIME: ${music.time.beats}/${music.time.beatUnit}")
        appendLine("$TEMPO: ${music.bpm}")
        append("$ARRANGEMENT: ${music.arrangement.joinToString(" ") { it.name }}".trimEnd())
    }

    /** Reads a key as its tonic and mode, as `F# Minor`. */
    private fun readKey(text: String): Key? {
        val parts = text.split(WHITESPACE)
        if (parts.size != 2) return null
        val (tonicText, modeText) = parts
        val diatonic = Diatonic.entries.firstOrNull { it.letter.equals(tonicText.firstOrNull() ?: return null, ignoreCase = true) }
            ?: return null
        val accidental = when (tonicText.length) {
            1 -> 0
            2 -> tonicText[1].toAccidental() ?: return null
            else -> return null
        }
        val mode = Mode.entries.firstOrNull { it.name.equals(modeText, ignoreCase = true) } ?: return null
        return Key(Chroma.of(diatonic.pitch + accidental), mode)
    }

    /** Reads a time signature, as `6/8`, whose beat unit is a power of two. */
    private fun readTime(text: String): TimeSignature? {
        val beats = text.substringBefore('/', "").trim().toIntOrNull()?.takeIf { it > 0 } ?: return null
        val beatUnit = text.substringAfter('/').trim().toIntOrNull()?.takeIf { it > 0 && it and (it - 1) == 0 }
            ?: return null
        return TimeSignature(beats, beatUnit)
    }
}

private const val KEY = "Key"
private const val TIME = "Time"
private const val TEMPO = "Tempo"
private const val ARRANGEMENT = "Arrangement"
private val WHITESPACE = Regex("\\s+")
