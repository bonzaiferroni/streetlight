package streetlight.model.data

/** The heading that opens the arrangement of a track's text. */
const val ARRANGEMENT_HEADING = "Arrangement:"

/** A problem found on [line] of a track's text, counted from 1. */
data class NotationError(
    val line: Int,
    val message: String,
)

/** The value read from a text, with the problems found where the text could not be read. */
data class NotationRead<out T>(
    val value: T,
    val errors: List<NotationError>,
)

/** A line of a track's text, numbered from 1. */
internal data class NotationLine(
    val number: Int,
    val text: String,
)

/** The heading of a pattern in a track's text and the lines of measures under it. */
internal data class PatternText(
    val patternId: PatternId,
    val lines: List<NotationLine>,
)

/** The patterns and arrangement of a track's text, before their measures are read. */
internal data class TrackText(
    val patterns: List<PatternText>,
    val arrangement: List<PatternId>,
)

/**
 * Reads the layout every track's text shares, reporting into [errors].
 *
 * A line holding `|` is a line of measures, under the heading before it. Any other line is a heading naming a pattern.
 * The line after [ARRANGEMENT_HEADING] lists the patterns in the order they play.
 */
internal fun readTrackText(text: String, errors: MutableList<NotationError>): TrackText {
    val patterns = mutableListOf<Pair<PatternId, MutableList<NotationLine>>>()
    val arrangementLines = mutableListOf<NotationLine>()
    var isArrangement = false

    text.lines().forEachIndexed { index, rawLine ->
        val line = NotationLine(index + 1, rawLine.trim())
        when {
            line.text.isEmpty() -> Unit
            line.text.equals(ARRANGEMENT_HEADING, ignoreCase = true) -> isArrangement = true
            isArrangement -> arrangementLines.add(line)
            '|' in line.text -> {
                val lines = patterns.lastOrNull()?.second
                if (lines == null) errors.add(NotationError(line.number, "Measures before a heading"))
                else lines.add(line)
            }
            else -> {
                val patternId = PatternId(line.text.removeSuffix(":").trim())
                if (patterns.any { it.first == patternId }) {
                    errors.add(NotationError(line.number, "A second pattern named ${patternId.name}"))
                } else {
                    patterns.add(patternId to mutableListOf())
                }
            }
        }
    }

    val arrangement = arrangementLines.flatMap { line ->
        line.text.split(WHITESPACE).mapNotNull { name ->
            val patternId = PatternId(name)
            if (patterns.any { it.first == patternId }) patternId
            else null.also { errors.add(NotationError(line.number, "No pattern named $name")) }
        }
    }
    return TrackText(patterns.map { (patternId, lines) -> PatternText(patternId, lines) }, arrangement)
}

/** A line of measures cut at its bar lines: the text before the first, if any, and each measure after it. */
internal data class BarLine(
    val lead: String?,
    val measures: List<String>,
)

internal fun NotationLine.toBarLine(): BarLine {
    val parts = text.split('|')
    val measures = parts.drop(1).let { if (it.lastOrNull()?.isBlank() == true) it.dropLast(1) else it }
    return BarLine(parts.first().trim().ifEmpty { null }, measures.map { it.trim() })
}

/** The tokens of [text], split on whitespace. */
internal fun tokensOf(text: String) = text.split(WHITESPACE).filter { it.isNotEmpty() }

/**
 * Writes a track's text: each pattern under its heading, its [PatternLines.pickup] before the first bar line, its
 * measures [PatternLines.measuresPerLine] to a line, then the [arrangement].
 */
internal fun writeTrackText(patterns: List<PatternLines>, arrangement: List<PatternId>) = buildString {
    for (pattern in patterns) {
        appendLine(pattern.patternId.name)
        val lines = pattern.measures.chunked(pattern.measuresPerLine.coerceAtLeast(1))
        if (lines.isEmpty() && pattern.pickup != null) appendLine("${pattern.pickup} |")
        lines.forEachIndexed { index, line ->
            val lead = pattern.pickup?.takeIf { index == 0 }?.let { "$it " }.orEmpty()
            appendLine("$lead| ${line.joinToString(" | ")} |")
        }
        appendLine()
    }
    appendLine(ARRANGEMENT_HEADING)
    append(arrangement.joinToString(" ") { it.name })
}

/** A pattern written as the text of its [pickup], or `null` for none, and of each of its [measures]. */
internal data class PatternLines(
    val patternId: PatternId,
    val pickup: String?,
    val measures: List<String>,
    val measuresPerLine: Int,
)

/** The measures on the first line of [lines], or [default] when there are none. */
internal fun measuresPerLineOf(lines: List<NotationLine>, default: Int) =
    lines.firstOrNull()?.toBarLine()?.measures?.size?.takeIf { it > 0 } ?: default

/** Reads a scale degree with its accidental before it, as `b7`, counted from the [Key.majorTonic] of [key]. */
internal fun degreePitchOf(degree: Int, accidental: Int, key: Key) =
    key.majorTonic.value + Diatonic.ofDegree(degree).pitch + accidental

internal fun Char.toAccidental() = when (this) {
    '#' -> 1
    'b' -> -1
    else -> null
}

/** Writes the scale degree of [semitones] above the tonic, with `#` before it for a pitch between degrees. */
internal fun writeDegree(semitones: Int) = Chromatic.ofPitch(semitones).let { chromatic ->
    if (chromatic.isSharp) "#${chromatic.diatonic.degree}" else chromatic.diatonic.degree.toString()
}

/** The beats of each of [count] even slots of [measure], summing to its length where it does not divide evenly. */
internal fun slotBeatsOf(count: Int, measure: Beats) =
    (0 until count).map { Beats(measure.ticks * (it + 1) / count - measure.ticks * it / count) }

/**
 * The tick of each slot of each measure the notes starting at [onsets] fill, the last onset being where they end.
 *
 * A measure is cut into the fewest even slots that hold every onset within it, so its text reads back to the same
 * notes.
 */
internal fun measureSlotsOf(onsets: List<Int>, measure: Beats): List<List<Int>> {
    val end = onsets.last()
    val measureTicks = measure.ticks
    val measureCount = (end + measureTicks - 1) / measureTicks
    return (0 until measureCount).map { measureIndex ->
        val measureStart = measureIndex * measureTicks
        val offsets = onsets.filter { it in measureStart until measureStart + measureTicks }.map { it - measureStart }
        val slot = offsets.fold(measureTicks) { divisor, offset -> gcd(divisor, offset) }
        (0 until measureTicks / slot).map { measureStart + it * slot }
    }
}

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private val WHITESPACE = Regex("\\s+")
