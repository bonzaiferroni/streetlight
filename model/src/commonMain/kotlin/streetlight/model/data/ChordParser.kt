package streetlight.model.data

/**
 * The parser of a chord track's text, with chords written in [style] against [key].
 *
 * Each measure's beats are shared evenly by its tokens: a chord, `.` to hold the note before, or `_` for a rest. The
 * tokens before the first bar line of a progression are its pickup, a beat each. A token that cannot be read is
 * reported and skipped.
 */
class ChordParser(
    private val key: Key,
    private val style: NotationStyle,
) {
    /** Reads a chord track from [text], with measures cut by [time]. */
    fun read(text: String, time: TimeSignature): NotationRead<ChordTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors)
        val progressions = trackText.patterns.map { pattern ->
            val notes = mutableListOf<ChordNote>()
            val pickup = mutableListOf<ChordNote>()
            pattern.lines.forEachIndexed { lineIndex, line ->
                fun error(message: String) = errors.add(NotationError(line.number, message))
                val barLine = line.toBarLine()
                barLine.lead?.let { lead ->
                    if (lineIndex == 0) tokensOf(lead).forEach { readToken(it, time.beat, pickup, ::error) }
                    else readMeasure(lead, time.measure, notes, ::error)
                }
                barLine.measures.forEach { readMeasure(it, time.measure, notes, ::error) }
            }
            Progression(pattern.patternId, notes, pickup, measuresPerLineOf(pattern.lines, MEASURES_PER_LINE))
        }
        return NotationRead(ChordTrack(progressions, trackText.placements), errors)
    }

    /** Writes [track] as the text [read] reads, with measures cut by [time]. */
    fun write(track: ChordTrack, time: TimeSignature): String {
        val patterns = track.patterns.map { progression ->
            PatternLines(
                patternId = progression.patternId,
                pickup = progression.pickup.takeIf { it.isNotEmpty() }?.let { writePickup(it, time.beat) },
                measures = writeMeasures(progression.notes, time.measure),
                measuresPerLine = progression.measuresPerLine,
            )
        }
        return writeTrackText(patterns, track.placements)
    }

    private fun readMeasure(measure: String, length: Beats, notes: MutableList<ChordNote>, error: (String) -> Unit) {
        val tokens = tokensOf(measure)
        if (tokens.isEmpty()) error("An empty measure")
        tokens.zip(slotBeatsOf(tokens.size, length)).forEach { (token, beats) -> readToken(token, beats, notes, error) }
    }

    private fun readToken(token: String, beats: Beats, notes: MutableList<ChordNote>, error: (String) -> Unit) {
        when (token) {
            HOLD -> {
                val last = notes.lastOrNull()
                if (last == null) error("A hold with no note before it")
                else notes[notes.lastIndex] = last.copy(beats = last.beats + beats)
            }
            REST -> notes.add(ChordNote(null, beats))
            else -> {
                val chord = readChord(token)
                if (chord == null) error("Unknown chord $token")
                else notes.add(ChordNote(chord, beats))
            }
        }
    }

    /**
     * Reads a chord written in [style], or returns `null`.
     *
     * Letters name the root, as `F#` or `Bb`. Degrees count from the [Key.majorTonic] of [key], with an accidental
     * before the degree, as `b7`. Either style takes the letter or symbol notation of a quality.
     */
    fun readChord(text: String): Chord? {
        var index = 0
        val root = when (style) {
            NotationStyle.Letters -> {
                val diatonic = Diatonic.entries.firstOrNull { it.letter == text.getOrNull(0) } ?: return null
                index = 1
                val accidental = text.getOrNull(index)?.toAccidental()
                if (accidental != null) index++
                Chroma.of(diatonic.pitch + (accidental ?: 0))
            }
            NotationStyle.Degrees -> {
                val accidental = text.getOrNull(0)?.toAccidental()
                if (accidental != null) index++
                val degree = text.getOrNull(index)?.digitToIntOrNull()?.takeIf { it in 1..7 } ?: return null
                index++
                Chroma.of(degreePitchOf(degree, accidental ?: 0, key))
            }
        }

        var rest = text.substring(index)
        var quality = ChordQuality.Major
        var seventh: Seventh? = null
        if (rest.startsWith(MAJOR_SEVENTH)) {
            seventh = Seventh.Major
            rest = rest.removePrefix(MAJOR_SEVENTH)
        } else {
            val match = QUALITY_NOTATIONS.firstOrNull { (notation, _) -> rest.startsWith(notation) }
            if (match != null) {
                quality = match.second
                rest = rest.removePrefix(match.first)
            }
        }
        if (seventh == null) {
            if (rest.startsWith(MAJOR_SEVENTH)) {
                seventh = Seventh.Major
                rest = rest.removePrefix(MAJOR_SEVENTH)
            } else if (rest.startsWith(SEVENTH)) {
                seventh = if (quality == ChordQuality.Diminished) Seventh.Diminished else Seventh.Minor
                rest = rest.removePrefix(SEVENTH)
            }
        }
        if (rest.isNotEmpty()) return null

        return Chord(root, quality, seventh, null, emptySet(), emptySet(), null)
    }

    /** Writes [pickup] a token per [beat], holding each note across the beats it fills. */
    private fun writePickup(pickup: List<ChordNote>, beat: Beats) = pickup.joinToString(" ") { note ->
        val count = (note.beats.ticks / beat.ticks).coerceAtLeast(1)
        (listOf(writeToken(note)) + List(count - 1) { HOLD }).joinToString(" ")
    }

    private fun writeMeasures(notes: List<ChordNote>, measure: Beats): List<String> {
        val onsets = notes.runningFold(0) { onset, note -> onset + note.beats.ticks }
        return measureSlotsOf(onsets, measure).map { slots ->
            slots.joinToString(" ") { tick ->
                val noteIndex = onsets.indexOf(tick)
                when {
                    tick >= onsets.last() -> REST
                    noteIndex < 0 -> HOLD
                    else -> writeToken(notes[noteIndex])
                }
            }
        }
    }

    private fun writeToken(note: ChordNote) = note.chord?.let { writeChord(it) } ?: REST

    /** Writes the root, quality and seventh of [chord] in [style] against [key]. */
    fun writeChord(chord: Chord) = buildString {
        when (style) {
            NotationStyle.Letters -> append(Chromatic.ofPitch(chord.root.value).label)
            NotationStyle.Degrees -> append(writeDegree(chord.root.value - key.majorTonic.value))
        }
        when {
            chord.quality == ChordQuality.Major -> Unit
            style == NotationStyle.Letters -> append(chord.quality.letterNotation)
            else -> append(chord.quality.symbolNotation)
        }
        when (chord.seventh) {
            Seventh.Major -> append(MAJOR_SEVENTH)
            Seventh.Minor, Seventh.Diminished -> append(SEVENTH)
            null -> Unit
        }
    }
}

/** The measures on each line of a progression when its text gives none. */
private const val MEASURES_PER_LINE = 4

/** Every notation of each quality, longest first so `maj` is not read as `m`. */
private val QUALITY_NOTATIONS = ChordQuality.entries
    .flatMap { listOf(it.letterNotation to it, it.symbolNotation to it) }
    .filter { it.first.isNotEmpty() }
    .sortedByDescending { it.first.length }

private const val HOLD = "."
private const val REST = "_"
private const val SEVENTH = "7"
private const val MAJOR_SEVENTH = "maj7"
