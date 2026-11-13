package streetlight.model.music.text

import streetlight.model.music.Beats
import streetlight.model.music.Interval
import streetlight.model.music.Key
import streetlight.model.music.MelodyNote
import streetlight.model.music.MelodyTrack
import streetlight.model.music.Phrase
import streetlight.model.music.SectionId
import streetlight.model.music.TimeSignature
import streetlight.model.music.MusicId
import streetlight.model.music.TrackId

/**
 * The parser of a melody track's jianpu, with degrees counted from the [Key.majorTonic] of [key].
 *
 * A note is a degree `1`–`7`, or `0` for a rest, a quarter note long in every meter. An accidental before the degree
 * raises or lowers it, and `'` or `,` after it moves it an octave up or down. Then `\` halves it, `\\` quarters it, and
 * `.` adds half its length, as `#4,\.`. `-` holds the note before for a quarter note, and `~` ties the next note to it.
 * The notes before the first bar line of a phrase are its pickup. A token that cannot be read is reported and skipped.
 *
 * A phrase's `-lyrics:` property names the stanzas sung to it, as `-lyrics: VerseB VerseC`.
 */
class MelodyParser(
    private val key: Key,
) {
    /**
     * Reads the melody track [trackId] of the music [musicId], named [name], from [text], with measures checked against
     * [time] and sections found in [names].
     */
    fun read(
        trackId: TrackId,
        musicId: MusicId,
        name: String,
        text: String,
        time: TimeSignature,
        names: SectionNames,
    ): NotationRead<MelodyTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors, names, setOf(LYRICS))
        val phrases = trackText.sections.map { section ->
            val lyrics = section.propertyOf(LYRICS)?.let { stanzas -> tokensOf(stanzas).map { names.idOf(it) } }
            readSection(section, time, errors).copy(lyrics = lyrics)
        }
        return NotationRead(MelodyTrack(trackId, musicId, name, phrases, trackText.arrangements, trackText.capo), errors)
    }

    /** Reads the section [sectionId] from [text], the lines of its measures alone, checked against [time]. */
    fun readSection(sectionId: SectionId, text: String, time: TimeSignature): NotationRead<Phrase> {
        val errors = mutableListOf<NotationError>()
        return NotationRead(readSection(sectionTextOf(sectionId, text, errors), time, errors), errors)
    }

    private fun readSection(section: SectionText, time: TimeSignature, errors: MutableList<NotationError>): Phrase {
        val notes = mutableListOf<MelodyNote>()
        val pickup = mutableListOf<MelodyNote>()
        var isTied = false
        val segments = section.toSegments()
        segments.pickup?.let { segment ->
            fun error(message: String) = errors.add(NotationError(segment.line, message))
            val read = readSegment(segment.text, QUARTER, pickup, isTied, ::error)
            isTied = read.isTied
            pickupProblemOf(read.ticks, time)?.let(::error)
        }
        segments.measures.forEachIndexed { index, segment ->
            fun error(message: String) = errors.add(NotationError(segment.line, message))
            val read = readSegment(segment.text, QUARTER, notes, isTied, ::error)
            isTied = read.isTied
            measureProblemOf(read.ticks, index == segments.measures.lastIndex, time)?.let(::error)
        }
        return Phrase(section.sectionId, notes, pickup, measuresPerLineOf(section.lines, MEASURES_PER_LINE))
    }

    /** The ticks a segment of jianpu filled, and whether it ends on a tie to the next note. */
    private data class SegmentRead(val ticks: Int, val isTied: Boolean)

    /** Reads the tokens of [text] onto the end of [notes], tying its first note to the last when [isTied]. */
    private fun readSegment(
        text: String,
        beat: Beats,
        notes: MutableList<MelodyNote>,
        isTied: Boolean,
        error: (String) -> Unit,
    ): SegmentRead {
        var isTied = isTied
        var ticks = 0
        for (token in tokensOf(text)) {
            when (token) {
                HOLD -> {
                    val last = notes.lastOrNull()
                    if (last?.interval == null) error("A hold with no note before it")
                    else {
                        notes[notes.lastIndex] = last.copy(beats = last.beats + beat)
                        ticks += beat.ticks
                    }
                }
                TIE -> if (notes.lastOrNull()?.interval == null) error("A tie with no note before it") else isTied = true
                else -> {
                    val note = readNote(token, beat)
                    if (note == null) {
                        error("Unknown note $token")
                        continue
                    }
                    ticks += note.beats.ticks
                    val last = notes.lastOrNull()
                    when {
                        !isTied -> notes.add(note)
                        last != null && last.interval == note.interval ->
                            notes[notes.lastIndex] = last.copy(beats = last.beats + note.beats)
                        else -> {
                            error("A tie between different pitches")
                            notes.add(note)
                        }
                    }
                    isTied = false
                }
            }
        }
        return SegmentRead(ticks, isTied)
    }

    private fun readNote(token: String, beat: Beats): MelodyNote? {
        val match = NOTE.matchEntire(token) ?: return null
        val (accidental, degreeText, octaves, length, dot) = match.destructured
        val beats = lengthOf(length, dot, beat)
        val degree = degreeText.toInt()
        if (degree == 0) return if (accidental.isEmpty() && octaves.isEmpty()) MelodyNote(null, beats) else null
        val octave = octaves.count { it == OCTAVE_UP } - octaves.count { it == OCTAVE_DOWN }
        val semitones = degreePitchOf(degree, accidental.firstOrNull()?.toAccidental() ?: 0, key)
        return MelodyNote(Interval.ofMidi(MIDDLE_C + semitones + octave * 12, key), beats)
    }

    /** Writes [track] as the jianpu [read] reads, with measures cut by [time] and sections named by [names]. */
    fun write(track: MelodyTrack, time: TimeSignature, names: SectionNames): String {
        val sections = track.sections.map { phrase ->
            val lyrics = phrase.lyrics?.let { lyrics -> mapOf(LYRICS to lyrics.joinToString(" ") { names.nameOf(it) }) }
            linesOf(phrase, time).copy(properties = lyrics.orEmpty())
        }
        return writeTrackText(sections, track.arrangements, track.capo, names)
    }

    /** Writes [phrase] as the jianpu [readSection] reads, with measures cut by [time]. */
    fun writeSection(phrase: Phrase, time: TimeSignature) = linesOf(phrase, time).toLines().joinToString("\n")

    private fun linesOf(phrase: Phrase, time: TimeSignature) = SectionLines(
        sectionId = phrase.sectionId,
        pickup = phrase.pickup.takeIf { it.isNotEmpty() }?.let { pickup ->
            pickup.flatMap { writeNote(it.interval, it.beats.ticks, QUARTER.ticks, false) }.joinToString(" ")
        },
        measures = writeMeasures(phrase.notes, time.measure, { it.beats.ticks }) { note, ticks, isContinuation ->
            writeNote(note.interval, ticks, QUARTER.ticks, isContinuation)
        },
        measuresPerLine = phrase.measuresPerLine,
    )

    /**
     * Writes [ticks] of [interval] as jianpu tokens: the note, `-` for each further beat, and a tied note for what is left
     * of a beat. A [continuation] carries on a note already written, so it opens with a hold or a tie.
     */
    private fun writeNote(interval: Interval?, ticks: Int, beat: Int, continuation: Boolean): List<String> {
        val degree = interval?.let { writeInterval(it) } ?: REST_DEGREE
        val wholeBeats = ticks / beat
        val fraction = ticks % beat
        val tokens = mutableListOf<String>()
        if (interval == null) {
            repeat(wholeBeats) { tokens.add(REST_DEGREE) }
            tokens.addAll(writeFraction(REST_DEGREE, fraction, beat))
            return tokens
        }
        if (!continuation && wholeBeats == 1 && fraction == beat / 2) return listOf("$degree$DOT")
        if (wholeBeats > 0) {
            if (continuation) repeat(wholeBeats) { tokens.add(HOLD) }
            else {
                tokens.add(degree)
                repeat(wholeBeats - 1) { tokens.add(HOLD) }
            }
        }
        val fractionTokens = writeFraction(degree, fraction, beat)
        if (fractionTokens.isNotEmpty()) {
            if (tokens.isNotEmpty() || continuation) tokens.add(TIE)
            fractionTokens.forEachIndexed { index, token ->
                if (index > 0) tokens.add(TIE)
                tokens.add(token)
            }
        }
        return tokens
    }

    /** Writes [ticks] shorter than a [beat] as eighths and sixteenths of [degree], longest first. */
    private fun writeFraction(degree: String, ticks: Int, beat: Int) =
        fractionSuffixesOf(ticks, beat).map { "$degree$it" }

    /** Writes [interval] as its degree, with an octave mark for each octave from the middle one. */
    private fun writeInterval(interval: Interval): String {
        val semitones = interval.toMidi(key) - MIDDLE_C - key.majorTonic.value
        val octave = semitones.floorDiv(12)
        val marks = if (octave > 0) OCTAVE_UP.toString().repeat(octave) else OCTAVE_DOWN.toString().repeat(-octave)
        return writeDegree(semitones.mod(12)) + marks
    }
}

/** The measures on each line of a phrase when its text gives none. */
private const val MEASURES_PER_LINE = 4

private val NOTE = Regex("""^([#b]?)([0-7])([',]*)(\\{0,2})(\.?)$""")
private const val MIDDLE_C = 60
private const val LYRICS = "lyrics"
private const val HOLD = "-"
private const val TIE = "~"
private const val REST_DEGREE = "0"
private const val OCTAVE_UP = '\''
private const val OCTAVE_DOWN = ','
