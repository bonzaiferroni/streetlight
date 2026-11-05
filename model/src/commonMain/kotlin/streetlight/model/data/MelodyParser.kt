package streetlight.model.data

/**
 * The parser of a melody track's jianpu, with degrees counted from the [Key.majorTonic] of [key].
 *
 * A note is a degree `1`–`7`, or `0` for a rest, a quarter note long. An accidental before the degree raises or lowers
 * it, and `'` or `,` after it moves it an octave up or down. Then `\` halves it, `\\` quarters it, and `.` adds half its
 * length, as `#4,\.`. `-` holds the note before for a beat, and `~` ties the next note to it. The notes before the first
 * bar line of a phrase are its pickup. A token that cannot be read is reported and skipped.
 */
class MelodyParser(
    private val key: Key,
) {
    /** Reads a melody track from [text], with measures checked against [time]. */
    fun read(text: String, time: TimeSignature): NotationRead<MelodyTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors)
        val phrases = trackText.sections.map { section ->
            val notes = mutableListOf<MelodyNote>()
            val pickup = mutableListOf<MelodyNote>()
            var isTied = false
            val measures = section.lines.flatMapIndexed { lineIndex, line ->
                val barLine = line.toBarLine()
                barLine.lead?.let { lead ->
                    if (lineIndex == 0) {
                        val read = readSegment(lead, time.beat, pickup, isTied) { errors.add(NotationError(line.number, it)) }
                        isTied = read.isTied
                        if (read.ticks >= time.measure.ticks) errors.add(NotationError(line.number, "A pickup of a measure or more"))
                    }
                }
                val leadMeasure = barLine.lead?.takeIf { lineIndex > 0 }
                (listOfNotNull(leadMeasure) + barLine.measures).map { it to line.number }
            }
            measures.forEachIndexed { index, (measure, lineNumber) ->
                fun error(message: String) = errors.add(NotationError(lineNumber, message))
                val read = readSegment(measure, time.beat, notes, isTied, ::error)
                isTied = read.isTied
                val isLast = index == measures.lastIndex
                if (read.ticks > time.measure.ticks || (read.ticks < time.measure.ticks && !isLast)) {
                    error("A measure of ${read.ticks.toBeatsText()} beats")
                }
            }
            Phrase(section.sectionId, notes, pickup, measuresPerLineOf(section.lines, MEASURES_PER_LINE))
        }
        return NotationRead(MelodyTrack(phrases, trackText.arrangements, trackText.capo), errors)
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
                    if (last?.pitch == null) error("A hold with no note before it")
                    else {
                        notes[notes.lastIndex] = last.copy(beats = last.beats + beat)
                        ticks += beat.ticks
                    }
                }
                TIE -> if (notes.lastOrNull()?.pitch == null) error("A tie with no note before it") else isTied = true
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
                        last != null && last.pitch == note.pitch ->
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
        val base = when (length) {
            EIGHTH -> beat.ticks / 2
            SIXTEENTH -> beat.ticks / 4
            else -> beat.ticks
        }
        val beats = Beats(if (dot.isEmpty()) base else base * 3 / 2)
        val degree = degreeText.toInt()
        if (degree == 0) return if (accidental.isEmpty() && octaves.isEmpty()) MelodyNote(null, beats) else null
        val octave = octaves.count { it == OCTAVE_UP } - octaves.count { it == OCTAVE_DOWN }
        val semitones = degreePitchOf(degree, accidental.firstOrNull()?.toAccidental() ?: 0, key)
        return MelodyNote(Pitch(MIDDLE_C + semitones + octave * 12), beats)
    }

    /** Writes [track] as the jianpu [read] reads, with measures cut by [time]. */
    fun write(track: MelodyTrack, time: TimeSignature): String {
        val sections = track.sections.map { phrase ->
            SectionLines(
                sectionId = phrase.sectionId,
                pickup = phrase.pickup.takeIf { it.isNotEmpty() }?.let { pickup ->
                    pickup.flatMap { writeNote(it.pitch, it.beats.ticks, time.beat.ticks, false) }.joinToString(" ")
                },
                measures = writeMeasures(phrase.notes, time),
                measuresPerLine = phrase.measuresPerLine,
            )
        }
        return writeTrackText(sections, track.arrangements, track.capo)
    }

    /** Writes [notes] as the jianpu of each measure they fill, holding a note across a bar line where it crosses one. */
    private fun writeMeasures(notes: List<MelodyNote>, time: TimeSignature): List<String> {
        val measureTicks = time.measure.ticks
        val measures = mutableListOf<MutableList<String>>()
        var tick = 0
        for (note in notes) {
            var remaining = note.beats.ticks
            var isContinuation = false
            while (remaining > 0) {
                if (tick % measureTicks == 0) measures.add(mutableListOf())
                val piece = minOf(remaining, measureTicks - tick % measureTicks)
                measures.last().addAll(writeNote(note.pitch, piece, time.beat.ticks, isContinuation))
                tick += piece
                remaining -= piece
                isContinuation = true
            }
        }
        return measures.map { it.joinToString(" ") }
    }

    /**
     * Writes [ticks] of [pitch] as jianpu tokens: the note, `-` for each further beat, and a tied note for what is left
     * of a beat. A [continuation] carries on a note already written, so it opens with a hold or a tie.
     */
    private fun writeNote(pitch: Pitch?, ticks: Int, beat: Int, continuation: Boolean): List<String> {
        val degree = pitch?.let { writePitch(it) } ?: REST_DEGREE
        val wholeBeats = ticks / beat
        val fraction = ticks % beat
        val tokens = mutableListOf<String>()
        if (pitch == null) {
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
    private fun writeFraction(degree: String, ticks: Int, beat: Int): List<String> {
        val tokens = mutableListOf<String>()
        var remaining = ticks
        for ((length, token) in listOf(beat * 3 / 4 to "$degree$EIGHTH$DOT", beat / 2 to "$degree$EIGHTH",
            beat * 3 / 8 to "$degree$SIXTEENTH$DOT", beat / 4 to "$degree$SIXTEENTH")) {
            while (remaining >= length) {
                tokens.add(token)
                remaining -= length
            }
        }
        return tokens
    }

    /** Writes [pitch] as its degree, with an octave mark for each octave from the middle one. */
    private fun writePitch(pitch: Pitch): String {
        val semitones = pitch.midi - MIDDLE_C - key.majorTonic.value
        val octave = semitones.floorDiv(12)
        val marks = if (octave > 0) OCTAVE_UP.toString().repeat(octave) else OCTAVE_DOWN.toString().repeat(-octave)
        return writeDegree(semitones.mod(12)) + marks
    }
}

/** The measures on each line of a phrase when its text gives none. */
private const val MEASURES_PER_LINE = 4

private fun Int.toBeatsText() = (this.toDouble() / Beats.TICKS_PER_BEAT).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

private val NOTE = Regex("""^([#b]?)([0-7])([',]*)(\\{0,2})(\.?)$""")
private const val MIDDLE_C = 60
private const val HOLD = "-"
private const val TIE = "~"
private const val DOT = "."
private const val EIGHTH = "\\"
private const val SIXTEENTH = "\\\\"
private const val REST_DEGREE = "0"
private const val OCTAVE_UP = '\''
private const val OCTAVE_DOWN = ','
