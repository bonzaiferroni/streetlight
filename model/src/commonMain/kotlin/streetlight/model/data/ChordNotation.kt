package streetlight.model.data

/** The measures written on each line of a progression. */
const val MEASURES_PER_LINE = 4

/** The heading that opens the placements of a track's text. */
const val PLACEMENTS_HEADING = "Placements:"

/** A problem found on [line] of a track's text, counted from 1. */
data class NotationError(
    val line: Int,
    val message: String,
)

/** The track read from a text, with the problems found where the text could not be read. */
data class ChordTrackRead(
    val track: ChordTrack,
    val errors: List<NotationError>,
)

/**
 * Reads a chord track from [text], with measures cut by [time] and chords written in [style] against [key].
 *
 * A heading line names a progression and the lines of measures that follow fill it. `|` divides measures, whose
 * beats are shared evenly by their tokens: a chord, `.` to hold the note before, or `_` for a rest. The line after
 * [PLACEMENTS_HEADING] lists the progressions in order. A token that cannot be read is reported and skipped.
 */
fun readChordTrack(text: String, time: TimeSignature, key: Key, style: NotationStyle): ChordTrackRead {
    val errors = mutableListOf<NotationError>()
    val progressions = mutableListOf<Pair<PatternId, MutableList<ChordNote>>>()
    val placements = mutableListOf<PatternId>()
    var isPlacements = false

    text.lines().forEachIndexed { index, rawLine ->
        val lineNumber = index + 1
        val line = rawLine.trim()
        fun error(message: String) = errors.add(NotationError(lineNumber, message))
        when {
            line.isEmpty() -> Unit
            line.equals(PLACEMENTS_HEADING, ignoreCase = true) -> isPlacements = true
            isPlacements -> line.split(WHITESPACE).forEach { name ->
                val patternId = PatternId(name)
                if (progressions.any { it.first == patternId }) placements.add(patternId)
                else error("No progression named $name")
            }
            line.startsWith('|') -> {
                val notes = progressions.lastOrNull()?.second
                if (notes == null) error("Measures before a heading")
                else readMeasures(line, time.measure, key, style, notes, ::error)
            }
            else -> {
                val patternId = PatternId(line.removeSuffix(":").trim())
                if (progressions.any { it.first == patternId }) error("A second progression named ${patternId.name}")
                else progressions.add(patternId to mutableListOf())
            }
        }
    }

    val track = ChordTrack(
        patterns = progressions.map { (patternId, notes) -> Progression(patternId, notes) },
        placements = placements,
    )
    return ChordTrackRead(track, errors)
}

/** Reads the measures of [line] onto the end of [notes], each [measure] long. */
private fun readMeasures(
    line: String,
    measure: Beats,
    key: Key,
    style: NotationStyle,
    notes: MutableList<ChordNote>,
    error: (String) -> Unit,
) {
    val measures = line.split('|').drop(1).let { if (it.lastOrNull()?.isBlank() == true) it.dropLast(1) else it }
    for (measureText in measures) {
        val tokens = measureText.trim().split(WHITESPACE).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) {
            error("An empty measure")
            continue
        }
        tokens.forEachIndexed { index, token ->
            // the slots share the measure's ticks, so a measure that does not divide evenly still sums to its length
            val beats = Beats(measure.ticks * (index + 1) / tokens.size - measure.ticks * index / tokens.size)
            when (token) {
                HOLD -> {
                    val last = notes.lastOrNull()
                    if (last == null) error("A hold with no note before it")
                    else notes[notes.lastIndex] = last.copy(beats = last.beats + beats)
                }
                REST -> notes.add(ChordNote(null, beats))
                else -> {
                    val chord = readChord(token, key, style)
                    if (chord == null) error("Unknown chord $token")
                    else notes.add(ChordNote(chord, beats))
                }
            }
        }
    }
}

/**
 * Reads a chord written in [style], or returns `null`.
 *
 * Letters name the root, as `F#` or `Bb`. Degrees count from the tonic of [key] in its major scale, with an
 * accidental before the degree, as `b7`. Either style takes the letter or symbol notation of a quality.
 */
fun readChord(text: String, key: Key, style: NotationStyle): Chord? {
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
            Chroma.of(key.tonic.value + Diatonic.ofDegree(degree).pitch + (accidental ?: 0))
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

/** Writes [track] as the text [readChordTrack] reads, with chords in [style] against [key]. */
fun writeChordTrack(track: ChordTrack, time: TimeSignature, key: Key, style: NotationStyle) = buildString {
    for (progression in track.patterns) {
        appendLine(progression.patternId.name)
        writeMeasures(progression.notes, time.measure, key, style)
            .chunked(MEASURES_PER_LINE)
            .forEach { line -> appendLine("| ${line.joinToString(" | ")} |") }
        appendLine()
    }
    appendLine(PLACEMENTS_HEADING)
    append(track.placements.joinToString(" ") { it.name })
}

/**
 * Writes [notes] as the text of each [measure] they fill.
 *
 * A measure is cut into the fewest even slots that hold every note's start, so the text reads back to the same notes.
 */
private fun writeMeasures(notes: List<ChordNote>, measure: Beats, key: Key, style: NotationStyle): List<String> {
    val starts = notes.runningFold(0) { onset, note -> onset + note.beats.ticks }
    val end = starts.last()
    val measureTicks = measure.ticks
    val measureCount = (end + measureTicks - 1) / measureTicks
    return (0 until measureCount).map { measureIndex ->
        val measureStart = measureIndex * measureTicks
        val offsets = starts.filter { it in measureStart until measureStart + measureTicks }.map { it - measureStart }
        val slot = offsets.fold(measureTicks) { divisor, offset -> gcd(divisor, offset) }
        (0 until measureTicks / slot).joinToString(" ") { slotIndex ->
            val tick = measureStart + slotIndex * slot
            val noteIndex = starts.indexOf(tick)
            when {
                tick >= end -> REST
                noteIndex < 0 -> HOLD
                else -> notes[noteIndex].chord?.let { writeChord(it, key, style) } ?: REST
            }
        }
    }
}

/** Writes the root, quality and seventh of [chord] in [style] against [key]. */
fun writeChord(chord: Chord, key: Key, style: NotationStyle) = buildString {
    when (style) {
        NotationStyle.Letters -> append(Chromatic.ofPitch(chord.root.value).label)
        NotationStyle.Degrees -> {
            val chromatic = Chromatic.ofPitch(chord.root.value - key.tonic.value)
            if (chromatic.isSharp) append('#')
            append(chromatic.diatonic.degree)
        }
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

private fun Char.toAccidental() = when (this) {
    '#' -> 1
    'b' -> -1
    else -> null
}

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

/** Every notation of each quality, longest first so `maj` is not read as `m`. */
private val QUALITY_NOTATIONS = ChordQuality.entries
    .flatMap { listOf(it.letterNotation to it, it.symbolNotation to it) }
    .filter { it.first.isNotEmpty() }
    .sortedByDescending { it.first.length }

private val WHITESPACE = Regex("\\s+")
private const val HOLD = "."
private const val REST = "_"
private const val SEVENTH = "7"
private const val MAJOR_SEVENTH = "maj7"
