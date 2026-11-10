package streetlight.model.music.text

import streetlight.model.music.Arrangement
import streetlight.model.music.Beats
import streetlight.model.music.Key
import streetlight.model.music.MusicSection
import streetlight.model.music.SectionId
import streetlight.model.music.UNNAMED_SECTION
import streetlight.model.music.TimeSignature

/** The heading that opens the arrangement of a track's text. */
const val ARRANGEMENT_HEADING = "Arrangement:"

/** The length of a plain note, a quarter note in every meter. */
internal val QUARTER = Beats.of(1)

/** The mark that adds half a note's length. */
internal const val DOT = "."

/** The mark that halves a note's length. */
internal const val EIGHTH = "\\"

/** The mark that quarters a note's length. */
internal const val SIXTEENTH = "\\\\"

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

/**
 * The [sections] of a music as a text names them. A text that names a section the music lacks gives it a new one, so
 * the sections are read back once every text of the music has been.
 */
class SectionNames(sections: List<MusicSection> = emptyList()) {
    private val held = sections.toMutableList()
    val sections: List<MusicSection> get() = held.toList()

    /** The section named [name], a new one when no section holds the name. */
    fun idOf(name: String) = findId(name) ?: SectionId.random().also { held.add(MusicSection(it, name)) }

    /** The section named [name], or `null` for none. */
    fun findId(name: String) = held.firstOrNull { it.name == name }?.sectionId

    fun nameOf(sectionId: SectionId) = held.firstOrNull { it.sectionId == sectionId }?.name ?: UNNAMED_SECTION
}

/** A line of a track's text, numbered from 1. */
internal data class NotationLine(
    val number: Int,
    val text: String,
)

/** The heading of a section in a track's text, the lines of measures under it and its [properties]. */
internal data class SectionText(
    val sectionId: SectionId,
    val lines: List<NotationLine>,
    val properties: List<PropertyLine> = emptyList(),
) {
    /** The value of the property [label], or `null` when the section does not state it. */
    fun propertyOf(label: String) = properties.firstOrNull { it.label.equals(label, ignoreCase = true) }?.value
}

/** A property line of a track's text, as `-capo: 2`: its [label] without the mark, and its [value]. */
internal data class PropertyLine(
    val label: String,
    val value: String,
)

/** The sections, arrangements and [capo] of a track's text, before the sections' measures are read. */
internal data class TrackText(
    val sections: List<SectionText>,
    val arrangements: List<Arrangement>,
    val capo: Int?,
)

/**
 * Reads the layout every track's text shares, reporting into [errors], its sections found by name in [names].
 *
 * A line ending in `:` without a bar line is a heading naming a section, and the lines under it are its measures, which
 * hold `|`. A label followed by names is an arrangement, as `Song: VerseA VerseB`, playing those sections in order. An
 * arrangement that names a missing section or holds itself is reported and left out.
 *
 * A label led by `-` is a property, as `-capo: 2`. It belongs to the section whose heading it follows, or to the track
 * when it follows none. A section's property is kept when [sectionLabels] names it, and reported otherwise.
 */
internal fun readTrackText(
    text: String,
    errors: MutableList<NotationError>,
    names: SectionNames,
    sectionLabels: Set<String> = emptySet(),
): TrackText {
    val headings = mutableListOf<HeadingText>()
    val arrangementLines = mutableListOf<ArrangementLine>()
    var heading: HeadingText? = null
    var capo: Int? = null
    text.lines().forEachIndexed { index, rawLine ->
        val line = NotationLine(index + 1, rawLine.trim())
        fun error(message: String) = errors.add(NotationError(line.number, message))
        val label = line.text.substringBefore(':', "").trim()
        val value = line.text.substringAfter(':').trim()
        when {
            line.text.isEmpty() -> Unit
            '|' in line.text -> heading?.measureLines?.add(line) ?: error("A line before any heading")
            ':' !in line.text -> error("A line with no bar line")
            label.startsWith(PROPERTY_MARK) -> {
                val property = PropertyLine(label.removePrefix(PROPERTY_MARK).trim(), value)
                val section = heading
                when {
                    section != null && sectionLabels.any { it.equals(property.label, ignoreCase = true) } ->
                        section.properties.add(property)
                    section == null && property.label.equals(CAPO, ignoreCase = true) ->
                        readCapo(value)?.let { capo = it } ?: error("Unknown capo $value")
                    else -> error("Unknown property ${property.label}")
                }
            }
            label.isEmpty() -> error("A heading with no name")
            else -> {
                val sectionId = names.idOf(label)
                when {
                    headings.any { it.sectionId == sectionId } || arrangementLines.any { it.sectionId == sectionId } ->
                        error("A second section named $label")
                    value.isEmpty() -> heading = HeadingText(sectionId).also { headings.add(it) }
                    else -> {
                        arrangementLines.add(ArrangementLine(sectionId, value, line.number))
                        heading = null
                    }
                }
            }
        }
    }

    val ids = (headings.map { it.sectionId } + arrangementLines.map { it.sectionId }).toSet()
    val arranged = arrangementLines.map { line ->
        val sections = tokensOf(line.names).mapNotNull { name ->
            names.findId(name)?.takeIf { it in ids }
                ?: null.also { errors.add(NotationError(line.number, "No section named $name")) }
        }
        line to Arrangement(line.sectionId, sections)
    }
    val arrangements = arranged.filter { (line, arrangement) ->
        val holdsItself = arrangement.sectionId.isReachedFrom(arrangement.sections, arranged.map { it.second })
        if (holdsItself) errors.add(NotationError(line.number, "${names.nameOf(arrangement.sectionId)} holds itself"))
        !holdsItself
    }.map { it.second }
    return TrackText(headings.map { SectionText(it.sectionId, it.measureLines, it.properties) }, arrangements, capo)
}

/** A heading of a track's text, the lines of measures under it and its properties, as they are read. */
private class HeadingText(val sectionId: SectionId) {
    val measureLines = mutableListOf<NotationLine>()
    val properties = mutableListOf<PropertyLine>()
}

/** A line of a track's text arranging the sections in [names] as [sectionId], numbered from 1. */
private data class ArrangementLine(
    val sectionId: SectionId,
    val names: String,
    val number: Int,
)

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

/**
 * The notation of the section [sectionId] alone, each line of [text] a line of its measures, reporting into [errors] a
 * line that holds no bar line.
 */
internal fun sectionTextOf(sectionId: SectionId, text: String, errors: MutableList<NotationError>): SectionText {
    val lines = text.lines().mapIndexed { index, line -> NotationLine(index + 1, line.trim()) }.filter { it.text.isNotEmpty() }
    lines.filter { '|' !in it.text }.forEach { errors.add(NotationError(it.number, "A line with no bar line")) }
    return SectionText(sectionId, lines.filter { '|' in it.text })
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

/** The text of a pickup or a measure and the [line] it was written on. */
internal data class SegmentText(
    val text: String,
    val line: Int,
)

/** A section's text cut into its [pickup], or `null` for none, and its [measures]. */
internal data class SectionSegments(
    val pickup: SegmentText?,
    val measures: List<SegmentText>,
)

/** Cuts this section into segments: the text before its first bar line is the pickup, and any other is a measure. */
internal fun SectionText.toSegments(): SectionSegments {
    val barLines = lines.map { it.number to it.toBarLine() }
    val pickup = barLines.firstOrNull()?.let { (number, barLine) -> barLine.lead?.let { SegmentText(it, number) } }
    val measures = barLines.flatMapIndexed { index, (number, barLine) ->
        (listOfNotNull(barLine.lead?.takeIf { index > 0 }) + barLine.measures).map { SegmentText(it, number) }
    }
    return SectionSegments(pickup, measures)
}

/** The problem with a pickup of [ticks] under [time], or `null` for none. */
internal fun pickupProblemOf(ticks: Int, time: TimeSignature) =
    "A pickup of a measure or more".takeIf { ticks >= time.measure.ticks }

/** The problem with a measure of [ticks] under [time], or `null` for none. Only the last measure may fall short. */
internal fun measureProblemOf(ticks: Int, isLast: Boolean, time: TimeSignature): String? {
    val measure = time.measure.ticks
    return "A measure of ${ticks.toBeatsText(time.beat)} beats".takeIf { ticks > measure || (ticks < measure && !isLast) }
}

/** The ticks counted in beats of [beat]. */
private fun Int.toBeatsText(beat: Beats) = (this.toDouble() / beat.ticks).let {
    if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
}

/** The length a note's [halving] and [dot] marks give it, a plain note being [beat] long. */
internal fun lengthOf(halving: String, dot: String, beat: Beats): Beats {
    val base = when (halving) {
        EIGHTH -> beat.ticks / 2
        SIXTEENTH -> beat.ticks / 4
        else -> beat.ticks
    }
    return Beats(if (dot.isEmpty()) base else base * 3 / 2)
}

/** The length marks of the pieces that fill [ticks] shorter than a [beat], longest first. */
internal fun fractionSuffixesOf(ticks: Int, beat: Int): List<String> {
    val suffixes = mutableListOf<String>()
    var remaining = ticks
    for ((length, suffix) in listOf(beat * 3 / 4 to "$EIGHTH$DOT", beat / 2 to EIGHTH,
        beat * 3 / 8 to "$SIXTEENTH$DOT", beat / 4 to SIXTEENTH)) {
        while (remaining >= length) {
            suffixes.add(suffix)
            remaining -= length
        }
    }
    return suffixes
}

/**
 * Writes [notes] as the text of each measure they fill, each piece of a note written by [write] with the ticks it holds
 * and whether it carries on a note already written, as it does past a bar line.
 */
internal fun <T> writeMeasures(
    notes: List<T>,
    measure: Beats,
    ticksOf: (T) -> Int,
    write: (T, Int, Boolean) -> List<String>,
): List<String> {
    val measures = mutableListOf<MutableList<String>>()
    var tick = 0
    for (note in notes) {
        var remaining = ticksOf(note)
        var isContinuation = false
        while (remaining > 0) {
            if (tick % measure.ticks == 0) measures.add(mutableListOf())
            val piece = minOf(remaining, measure.ticks - tick % measure.ticks)
            measures.last().addAll(write(note, piece, isContinuation))
            tick += piece
            remaining -= piece
            isContinuation = true
        }
    }
    return measures.map { it.joinToString(" ") }
}

/** The tokens of [text], split on whitespace. */
internal fun tokensOf(text: String) = text.split(WHITESPACE).filter { it.isNotEmpty() }

/**
 * Writes a track's text: each section under its heading, named by [names], its [SectionLines.pickup] before the first
 * bar line, its measures [SectionLines.measuresPerLine] to a line and its properties after them, then each of the
 * [arrangements] under its own. A [capo] opens the text when there is one.
 */
internal fun writeTrackText(
    sections: List<SectionLines>,
    arrangements: List<Arrangement>,
    capo: Int?,
    names: SectionNames,
) = writeTrackBodies(
    bodies = sections.map { SectionBody(it.sectionId, it.toLines() + it.toPropertyLines()) },
    arrangements = arrangements,
    capo = capo,
    names = names,
)

/** Writes a track's text from the [bodies] of its sections, each its heading and lines, then its [arrangements]. */
internal fun writeTrackBodies(
    bodies: List<SectionBody>,
    arrangements: List<Arrangement>,
    capo: Int?,
    names: SectionNames,
) = buildString {
    capo?.let {
        appendLine("$PROPERTY_MARK$CAPO: $it")
        appendLine()
    }
    for (body in bodies) {
        appendLine("${names.nameOf(body.sectionId)}:")
        body.lines.forEach { appendLine(it) }
        appendLine()
    }
    for (arrangement in arrangements) {
        appendLine("${names.nameOf(arrangement.sectionId)}: ${arrangement.sections.joinToString(" ") { names.nameOf(it) }}")
        appendLine()
    }
}.trimEnd()

/** The lines written under the heading of a section. */
internal data class SectionBody(
    val sectionId: SectionId,
    val lines: List<String>,
)

internal fun SectionLines.toLines(): List<String> {
    val lines = measures.chunked(measuresPerLine.coerceAtLeast(1))
    if (lines.isEmpty()) return listOfNotNull(pickup?.let { "$it |" })
    return lines.mapIndexed { index, line ->
        val lead = pickup?.takeIf { index == 0 }?.let { "$it " }.orEmpty()
        "$lead| ${line.joinToString(" | ")} |"
    }
}

private fun SectionLines.toPropertyLines() =
    properties.map { (label, value) -> "$PROPERTY_MARK$label: $value".trimEnd() }

/**
 * A section written as the text of its [pickup], or `null` for none, and of each of its [measures], with the value of
 * each of its [properties] by its label.
 */
internal data class SectionLines(
    val sectionId: SectionId,
    val pickup: String?,
    val measures: List<String>,
    val measuresPerLine: Int,
    val properties: Map<String, String> = emptyMap(),
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

/** Reads a capo, a shift of up to 12 semitones up or down, or returns `null`. */
internal fun readCapo(text: String) = text.toIntOrNull()?.takeIf { it in -MAX_CAPO..MAX_CAPO }

/** The mark that leads the label of a property. */
private const val PROPERTY_MARK = "-"

private const val CAPO = "capo"
private const val MAX_CAPO = 12

internal tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private val WHITESPACE = Regex("\\s+")
