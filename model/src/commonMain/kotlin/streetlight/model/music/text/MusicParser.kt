package streetlight.model.music.text

import streetlight.model.music.Chroma
import streetlight.model.music.Key
import streetlight.model.music.Mode
import streetlight.model.music.Music
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.TimeSignature

/**
 * The parser of a song's music written as labeled lines, as `Key: C Major`, `Time: 3/4`, `Tempo: 90` and `Capo: 2`,
 * then `Arrangement:` over an indented line for each section it plays, as `Verse A: 16`, its name and measures.
 *
 * A label is read in any case. A line left out keeps the value it had, and a line that cannot be read is reported. The
 * arrangement holds the music's sections, each measured where it first plays. A later line may name it alone, and a
 * section keeps the id of the music's section of its name.
 */
object MusicParser {
    /** Reads [text] onto [music]. */
    fun read(text: String, music: Music): NotationRead<Music> {
        val errors = mutableListOf<NotationError>()
        var result = music
        var arrangementRead: ArrangementRead? = null
        val arrangements = mutableListOf<ArrangementRead>()
        text.lines().forEachIndexed { index, rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed
            fun error(message: String) = errors.add(NotationError(index + 1, message))
            val arrangement = arrangementRead
            if (arrangement != null && rawLine.first().isWhitespace()) return@forEachIndexed arrangement.readLine(line, ::error)
            arrangementRead = null
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
                label.equals(CAPO, ignoreCase = true) -> if (value.isEmpty()) result = result.copy(capo = null)
                    else readCapo(value)?.let { result = result.copy(capo = it) } ?: error("Unknown capo $value")
                label.equals(ARRANGEMENT, ignoreCase = true) ->
                    if (value.isEmpty()) arrangementRead = ArrangementRead(music.sections).also { arrangements.add(it) }
                    else error("An arrangement lists a section on each indented line below it")
                else -> error("Unknown line $line")
            }
        }
        arrangements.lastOrNull()?.let { result = result.copy(sections = it.sections, arrangement = it.arrangement) }
        return NotationRead(result, errors)
    }

    /** Writes [music] as the text [read] reads. */
    fun write(music: Music) = buildString {
        appendLine("$KEY: ${Chromatic.ofPitch(music.key.tonic.value).label} ${music.key.mode.name}")
        appendLine("$TIME: ${music.time.beats}/${music.time.beatUnit}")
        appendLine("$TEMPO: ${music.bpm}")
        appendLine("$CAPO: ${music.capo ?: ""}".trimEnd())
        append("$ARRANGEMENT:")
        val measured = mutableSetOf<SectionId>()
        music.arrangement.forEach { sectionId ->
            val section = music.sections.firstOrNull { it.sectionId == sectionId }
            appendLine()
            append(ARRANGEMENT_INDENT)
            if (section == null || !measured.add(sectionId)) append(music.nameOf(sectionId))
            else append("${section.name}: ${section.measures}")
        }
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

/** The sections of an arrangement as its lines are read, each given the id of the section of its name in [held]. */
private class ArrangementRead(private val held: List<Section>) {
    val sections = mutableListOf<Section>()
    val arrangement = mutableListOf<SectionId>()

    /** Reads [line], a section's name and its measures, or its name alone after its first, reporting to [error]. */
    fun readLine(line: String, error: (String) -> Unit) {
        val name = line.substringBefore(':').trim()
        val measuresText = line.substringAfter(':', "").trim()
        val section = sections.firstOrNull { it.name == name }
        val measures = measuresText.toIntOrNull()?.takeIf { it > 0 }
        when {
            name.isEmpty() -> error("A section with no name")
            ',' in name -> error("A name with a comma: $name")
            measuresText.isNotEmpty() && measures == null -> error("Unknown measures $measuresText")
            section != null && measures != null && measures != section.measures ->
                error("$name was measured ${section.measures} before")
            section != null -> arrangement.add(section.sectionId)
            measures == null -> error("$name needs its measures where it first plays, as $name: 16")
            else -> {
                val sectionId = held.firstOrNull { it.name == name }?.sectionId ?: SectionId.random()
                sections.add(Section(sectionId, name, measures))
                arrangement.add(sectionId)
            }
        }
    }
}

private const val KEY = "Key"
private const val TIME = "Time"
private const val TEMPO = "Tempo"
private const val ARRANGEMENT = "Arrangement"
private const val CAPO = "Capo"
private val WHITESPACE = Regex("\\s+")
private const val ARRANGEMENT_INDENT = "    "
