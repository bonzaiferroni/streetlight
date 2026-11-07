package streetlight.model.music.text

import streetlight.model.music.Beats
import streetlight.model.music.LyricNote
import streetlight.model.music.LyricTrack
import streetlight.model.music.Stanza
import streetlight.model.music.TimeSignature

/**
 * The parser of a lyric track's text.
 *
 * Each measure's beats are shared evenly by its tokens: a syllable or word, `.` to hold the one before, or `_` for a
 * rest. A syllable keeps the `-` that joins it to the next. The tokens before the first bar line of a stanza are its
 * pickup, a beat each.
 */
object LyricParser {
    /** Reads a lyric track from [text], with measures cut by [time]. */
    fun read(text: String, time: TimeSignature): NotationRead<LyricTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors)
        val stanzas = trackText.sections.map { section ->
            val notes = mutableListOf<LyricNote>()
            val pickup = mutableListOf<LyricNote>()
            section.lines.forEachIndexed { lineIndex, line ->
                fun error(message: String) = errors.add(NotationError(line.number, message))
                val barLine = line.toBarLine()
                barLine.lead?.let { lead ->
                    if (lineIndex == 0) tokensOf(lead).forEach { readToken(it, time.beat, pickup, ::error) }
                    else readMeasure(lead, time.measure, notes, ::error)
                }
                barLine.measures.forEach { readMeasure(it, time.measure, notes, ::error) }
            }
            Stanza(section.sectionId, notes, pickup, measuresPerLineOf(section.lines, MEASURES_PER_LINE))
        }
        return NotationRead(LyricTrack(stanzas, trackText.arrangements, trackText.capo), errors)
    }

    /** Writes [track] as the text [read] reads, with measures cut by [time]. */
    fun write(track: LyricTrack, time: TimeSignature): String {
        val sections = track.sections.map { stanza ->
            SectionLines(
                sectionId = stanza.sectionId,
                pickup = stanza.pickup.takeIf { it.isNotEmpty() }?.let { writePickup(it, time.beat) },
                measures = writeMeasures(stanza.notes, time.measure),
                measuresPerLine = stanza.measuresPerLine,
            )
        }
        return writeTrackText(sections, track.arrangements, track.capo)
    }

    private fun readMeasure(measure: String, length: Beats, notes: MutableList<LyricNote>, error: (String) -> Unit) {
        val tokens = tokensOf(measure)
        if (tokens.isEmpty()) error("An empty measure")
        tokens.zip(slotBeatsOf(tokens.size, length)).forEach { (token, beats) -> readToken(token, beats, notes, error) }
    }

    private fun readToken(token: String, beats: Beats, notes: MutableList<LyricNote>, error: (String) -> Unit) {
        when (token) {
            HOLD -> {
                val last = notes.lastOrNull()
                if (last == null) error("A hold with no note before it")
                else notes[notes.lastIndex] = last.copy(beats = last.beats + beats)
            }
            REST -> notes.add(LyricNote(null, beats))
            else -> notes.add(LyricNote(token, beats))
        }
    }

    /** Writes [pickup] a token per [beat], holding each note across the beats it fills. */
    private fun writePickup(pickup: List<LyricNote>, beat: Beats) = pickup.joinToString(" ") { note ->
        val count = (note.beats.ticks / beat.ticks).coerceAtLeast(1)
        (listOf(note.text ?: REST) + List(count - 1) { HOLD }).joinToString(" ")
    }

    private fun writeMeasures(notes: List<LyricNote>, measure: Beats): List<String> {
        val onsets = notes.runningFold(0) { onset, note -> onset + note.beats.ticks }
        return measureSlotsOf(onsets, measure).map { slots ->
            slots.joinToString(" ") { tick ->
                val noteIndex = onsets.indexOf(tick)
                when {
                    tick >= onsets.last() -> REST
                    noteIndex < 0 -> HOLD
                    else -> notes[noteIndex].text ?: REST
                }
            }
        }
    }
}

/** The measures on each line of a stanza when its text gives none. */
private const val MEASURES_PER_LINE = 2
private const val HOLD = "."
private const val REST = "_"
