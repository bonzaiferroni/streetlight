package streetlight.model.data

/** The heading that opens the arrangement of a track's text. */
const val ARRANGEMENT_HEADING = "Arrangement:"

/** A problem found on [line] of a text, counted from 1. */
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

/** The heading of a section in a track's text and the lines of measures under it. */
internal data class SectionText(
    val sectionId: SectionId,
    val lines: List<NotationLine>,
)

/** The sections, arrangements and [capo] of a track's text, before the sections' measures are read. */
internal data class TrackText(
    val sections: List<SectionText>,
    val arrangements: List<Arrangement>,
    val capo: Int?,
)

/**
 * Reads the layout every track's text shares, reporting into [errors].
 *
 * The text opens with the track's properties, each a labeled line such as `Capo: 2`. After them, a line ending in `:`
 * without a bar line is a heading naming a section. The lines under it are lines of measures,
 * which hold `|`, or the sections of an arrangement, which list the sections it plays in order. An arrangement that
 * names a missing section or holds itself is reported and left out.
 */
internal fun readTrackText(text: String, errors: MutableList<NotationError>): TrackText {
    val headings = mutableListOf<HeadingText>()
    var capo: Int? = null
    text.lines().forEachIndexed { index, rawLine ->
        val line = NotationLine(index + 1, rawLine.trim())
        fun error(message: String) = errors.add(NotationError(line.number, message))
        val heading = headings.lastOrNull()
        when {
            line.text.isEmpty() -> Unit
            '|' !in line.text && line.text.endsWith(':') -> {
                val sectionId = SectionId(line.text.removeSuffix(":").trim())
                if (headings.any { it.sectionId == sectionId }) error("A second section named ${sectionId.name}")
                else headings.add(HeadingText(sectionId, line.number))
            }
            heading == null -> {
                val label = line.text.substringBefore(':', "").trim()
                val value = line.text.substringAfter(':').trim()
                when {
                    label.equals(CAPO, ignoreCase = true) ->
                        readCapo(value)?.let { capo = it } ?: error("Unknown capo $value")
                    else -> error("A line before any heading")
                }
            }
            '|' in line.text -> heading.measureLines.add(line)
            heading.measureLines.isNotEmpty() -> error("An arrangement line among measures")
            else -> heading.arrangementLines.add(line)
        }
    }

    val ids = headings.map { it.sectionId }.toSet()
    val arranged = headings.filter { it.arrangementLines.isNotEmpty() && it.measureLines.isEmpty() }.map { heading ->
        val sections = heading.arrangementLines.flatMap { line ->
            tokensOf(line.text).mapNotNull { name ->
                SectionId(name).takeIf { it in ids }
                    ?: null.also { errors.add(NotationError(line.number, "No section named $name")) }
            }
        }
        heading to Arrangement(heading.sectionId, sections)
    }
    val arrangements = arranged.filter { (heading, arrangement) ->
        val holdsItself = arrangement.sectionId.isReachedFrom(arrangement.sections, arranged.map { it.second })
        if (holdsItself) errors.add(NotationError(heading.number, "${arrangement.sectionId.name} holds itself"))
        !holdsItself
    }.map { it.second }
    val arrangedIds = arrangements.map { it.sectionId }.toSet()
    val sections = headings.filter { it.sectionId !in arrangedIds && it.arrangementLines.isEmpty() }
        .map { SectionText(it.sectionId, it.measureLines) }
    return TrackText(sections, arrangements, capo)
}

/** A heading of a track's text and the lines under it, as they are read. */
private class HeadingText(val sectionId: SectionId, val number: Int) {
    val measureLines = mutableListOf<NotationLine>()
    val arrangementLines = mutableListOf<NotationLine>()
}

/** Whether this section is reached by following [sectionIds] through [arrangements]. */
private fun SectionId.isReachedFrom(sectionIds: List<SectionId>, arrangements: List<Arrangement>): Boolean {
    val visited = mutableSetOf<SectionId>()
    val pending = ArrayDeque(sectionIds)
    while (pending.isNotEmpty()) {
        val next = pending.removeFirst()
        if (next == this) return true
        if (!visited.add(next)) continue
        arrangements.firstOrNull { it.sectionId == next }?.let { pending.addAll(it.sections) }
    }
    return false
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
 * Writes a track's text: each section under its heading, its [SectionLines.pickup] before the first bar line and its
 * measures [SectionLines.measuresPerLine] to a line, then each of the [arrangements] under its own. A [capo] opens
 * the text when there is one.
 */
internal fun writeTrackText(sections: List<SectionLines>, arrangements: List<Arrangement>, capo: Int?) = buildString {
    capo?.let {
        appendLine("$CAPO: $it")
        appendLine()
    }
    for (section in sections) {
        appendLine("${section.sectionId.name}:")
        val lines = section.measures.chunked(section.measuresPerLine.coerceAtLeast(1))
        if (lines.isEmpty() && section.pickup != null) appendLine("${section.pickup} |")
        lines.forEachIndexed { index, line ->
            val lead = section.pickup?.takeIf { index == 0 }?.let { "$it " }.orEmpty()
            appendLine("$lead| ${line.joinToString(" | ")} |")
        }
        appendLine()
    }
    for (arrangement in arrangements) {
        appendLine("${arrangement.sectionId.name}:")
        appendLine(arrangement.sections.joinToString(" ") { it.name })
        appendLine()
    }
}.trimEnd()

/** A section written as the text of its [pickup], or `null` for none, and of each of its [measures]. */
internal data class SectionLines(
    val sectionId: SectionId,
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

/** Reads a capo, a fret from 0 to 12, or returns `null`. */
internal fun readCapo(text: String) = text.toIntOrNull()?.takeIf { it in 0..MAX_CAPO }

private const val CAPO = "Capo"
private const val MAX_CAPO = 12

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private val WHITESPACE = Regex("\\s+")
