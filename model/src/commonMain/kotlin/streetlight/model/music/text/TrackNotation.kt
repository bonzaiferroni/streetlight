package streetlight.model.music.text

import streetlight.model.music.Arrangement
import streetlight.model.music.Beats
import streetlight.model.music.Key
import streetlight.model.music.NotationId
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.TimeSignature
import streetlight.model.music.Track
import streetlight.model.music.UNNAMED_SECTION
import streetlight.model.music.sectionIdsOf

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
data class NotationParse<out T>(
    val value: T,
    val errors: List<NotationError>,
)

/** The [sections] of a music as a text names them. */
class SectionNames(private val sections: List<Section>) {
    /** The section named [name], or `null` for none. */
    fun findId(name: String) = sections.firstOrNull { it.name == name }?.sectionId

    fun nameOf(sectionId: SectionId) = sections.firstOrNull { it.sectionId == sectionId }?.name ?: UNNAMED_SECTION
}

/** A line of a track's text, numbered from 1. */
internal data class NotationLine(
    val number: Int,
    val text: String,
)

/** A notation of a track's text, named [name] by its heading or unnamed for none, and the lines of measures under it. */
internal data class NotationText(
    val notationId: NotationId,
    val name: String?,
    val lines: List<NotationLine>,
)

/** A property line of a track's text, as `-capo: 2`: its [label] without the mark, and its [value], on [line]. */
internal data class PropertyLine(
    val label: String,
    val value: String,
    val line: Int,
)

/**
 * The notations, arrangements and [capo] of a track's text, before the notations' measures are read, with the
 * notation or arrangement it plays for each of its [sections] and the track's other [properties].
 */
internal data class TrackText(
    val notations: List<NotationText>,
    val arrangements: List<Arrangement>,
    val sections: Map<SectionId, NotationId>,
    val capo: Int?,
    val properties: List<PropertyLine>,
) {
    /** The property [label] of the track, or `null` when the text does not state it. */
    fun propertyOf(label: String) = properties.firstOrNull { it.label.equals(label, ignoreCase = true) }
}

/**
 * Reads the layout every track's text shares, reporting into [errors], with the sections of its music found in
 * [names].
 *
 * A line ending in `:` without a bar line is a heading naming a notation, and the lines under it are its measures,
 * which hold `|`. Measures before the first heading are the track's one unnamed notation. A name followed by names is
 * an arrangement, as `Song: Verse A, Verse B`, playing those notations in order. An arrangement that names a missing
 * notation or holds itself is reported and left out. A name holds no comma.
 *
 * A label led by `-` is a property, as `-capo: 2`. The track's come before its first notation, and [trackLabels]
 * names those it keeps besides its capo. A notation or arrangement states the sections it plays with `-sections:`
 * after its measures. Without one, a notation no arrangement holds plays the section of its name. A section played
 * twice is reported and played by the first.
 */
internal fun readTrackText(
    text: String,
    errors: MutableList<NotationError>,
    names: SectionNames,
    trackLabels: Set<String> = emptySet(),
): TrackText {
    val parts = mutableListOf<PartText>()
    var part: PartText? = null
    var capo: Int? = null
    val properties = mutableListOf<PropertyLine>()
    text.lines().forEachIndexed { index, rawLine ->
        val line = NotationLine(index + 1, rawLine.trim())
        fun error(message: String) = errors.add(NotationError(line.number, message))
        val label = line.text.substringBefore(':', "").trim()
        val value = line.text.substringAfter(':').trim()
        val current = part
        when {
            line.text.isEmpty() -> Unit
            '|' in line.text -> when {
                current is NotationPart -> current.measureLines.add(line)
                current == null -> part = NotationPart(null, line.number).also {
                    it.measureLines.add(line)
                    parts.add(it)
                }
                else -> error("Measures under an arrangement")
            }
            ':' !in line.text -> error("A line with no bar line")
            label.startsWith(PROPERTY_MARK) -> {
                val property = PropertyLine(label.removePrefix(PROPERTY_MARK).trim(), value, line.number)
                when {
                    current != null && property.label.equals(SECTIONS, ignoreCase = true) -> current.sectionsLine = property
                    current == null && property.label.equals(CAPO, ignoreCase = true) ->
                        readCapo(value)?.let { capo = it } ?: error("Unknown capo $value")
                    current == null && trackLabels.any { it.equals(property.label, ignoreCase = true) } ->
                        properties.add(property)
                    else -> error("Unknown property ${property.label}")
                }
            }
            label.isEmpty() -> error("A heading with no name")
            ',' in label -> error("A name with a comma: $label")
            parts.any { it.name == label } -> error("A second notation named $label")
            value.isEmpty() -> part = NotationPart(label, line.number).also { parts.add(it) }
            else -> part = ArrangementPart(label, value, line.number).also { parts.add(it) }
        }
    }

    // read each arrangement, leaving out one that holds itself
    val ids = parts.associateWith { NotationId.random() }
    val idsByName = parts.mapNotNull { part -> part.name?.let { it to ids.getValue(part) } }.toMap()
    val arranged = parts.filterIsInstance<ArrangementPart>().map { part ->
        val held = namesOf(part.notations).mapNotNull { name ->
            idsByName[name] ?: null.also { errors.add(NotationError(part.line, "No notation named $name")) }
        }
        part to Arrangement(ids.getValue(part), part.label, held)
    }
    val arrangements = arranged.filter { (part, arrangement) ->
        val holdsItself = arrangement.notationId.isReachedFrom(arrangement.notations, arranged.map { it.second })
        if (holdsItself) errors.add(NotationError(part.line, "${part.label} holds itself"))
        !holdsItself
    }

    // claim the sections each part plays: those it names, or the section of its own name when no arrangement holds it
    val kept = parts.filter { it is NotationPart || arrangements.any { (kept) -> kept == it } }
    val heldIds = arrangements.flatMap { it.second.notations }.toSet()
    val claims = linkedMapOf<SectionId, PartText>()
    fun claim(sectionId: SectionId, part: PartText, line: Int) {
        val holder = claims[sectionId]
        if (holder == null) claims[sectionId] = part
        else errors.add(NotationError(line, "${names.nameOf(sectionId)} is played by both ${holder.label} and ${part.label}"))
    }
    kept.forEach { part ->
        val sectionsLine = part.sectionsLine
        when {
            sectionsLine != null -> namesOf(sectionsLine.value).forEach { name ->
                names.findId(name)?.let { claim(it, part, sectionsLine.line) }
                    ?: errors.add(NotationError(sectionsLine.line, "No section named $name"))
            }
            ids.getValue(part) !in heldIds -> part.name?.let { names.findId(it) }?.let { claim(it, part, part.line) }
        }
    }

    return TrackText(
        notations = kept.filterIsInstance<NotationPart>().map { NotationText(ids.getValue(it), it.name, it.measureLines) },
        arrangements = arrangements.map { it.second },
        sections = claims.mapValues { (_, part) -> ids.getValue(part) },
        capo = capo,
        properties = properties,
    )
}

/** A notation or arrangement of a track's text as it is read, headed on [line] and named [name], or unnamed for none. */
private sealed class PartText(val name: String?, val line: Int) {
    var sectionsLine: PropertyLine? = null

    /** The name a problem gives it. */
    val label get() = name ?: "the unnamed notation"
}

/** A notation of a track's text and the lines of measures under it, as they are read. */
private class NotationPart(name: String?, line: Int) : PartText(name, line) {
    val measureLines = mutableListOf<NotationLine>()
}

/** An arrangement of a track's text, playing the [notations] it names in order, as it is read. */
private class ArrangementPart(name: String, val notations: String, line: Int) : PartText(name, line)

/** Whether this notation is reached by following [notationIds] through [arrangements]. */
private fun NotationId.isReachedFrom(notationIds: List<NotationId>, arrangements: List<Arrangement>): Boolean {
    val visited = mutableSetOf<NotationId>()
    val pending = ArrayDeque(notationIds)
    while (pending.isNotEmpty()) {
        val next = pending.removeFirst()
        if (next == this) return true
        if (!visited.add(next)) continue
        arrangements.firstOrNull { it.notationId == next }?.let { pending.addAll(it.notations) }
    }
    return false
}

/**
 * The notation [notationId] named [name] alone, each line of [text] a line of its measures, reporting into [errors] a
 * line that holds no bar line.
 */
internal fun notationTextOf(
    notationId: NotationId,
    name: String?,
    text: String,
    errors: MutableList<NotationError>,
): NotationText {
    val lines = text.lines().mapIndexed { index, line -> NotationLine(index + 1, line.trim()) }.filter { it.text.isNotEmpty() }
    lines.filter { '|' !in it.text }.forEach { errors.add(NotationError(it.number, "A line with no bar line")) }
    return NotationText(notationId, name, lines.filter { '|' in it.text })
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

/** A notation's text cut into its [pickup], or `null` for none, and its [measures]. */
internal data class SectionSegments(
    val pickup: SegmentText?,
    val measures: List<SegmentText>,
)

/** Cuts this notation into segments: the text before its first bar line is the pickup, and any other is a measure. */
internal fun NotationText.toSegments(): SectionSegments {
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

/** The names listed in [text], split on commas. */
internal fun namesOf(text: String) = text.split(NAME_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

/**
 * Writes the text of [track]: its capo and its other [properties], then each of its notations from [lines], the
 * unnamed one first and without a heading, then each of its arrangements, sections named by [names].
 */
internal fun writeTrackText(
    track: Track,
    lines: List<NotationLines>,
    names: SectionNames,
    properties: Map<String, String> = emptyMap(),
) = writeTrackBodies(track, lines.map { NotationBody(it.notationId, it.name, it.toLines()) }, names, properties)

/**
 * Writes the text of [track] as [writeTrackText] does, from the [bodies] of its notations. A notation or arrangement
 * states the sections it plays after its lines, unless its name plays them alone.
 */
internal fun writeTrackBodies(
    track: Track,
    bodies: List<NotationBody>,
    names: SectionNames,
    properties: Map<String, String> = emptyMap(),
) = buildString {
    val propertyLines = listOfNotNull(track.capo?.let { "$PROPERTY_MARK$CAPO: $it" }) +
        properties.map { (label, value) -> "$PROPERTY_MARK$label: $value".trimEnd() }
    if (propertyLines.isNotEmpty()) {
        propertyLines.forEach { appendLine(it) }
        appendLine()
    }

    val heldIds = track.arrangements.flatMap { it.notations }.toSet()
    fun appendSections(notationId: NotationId, name: String?) {
        val sectionIds = track.sectionIdsOf(notationId)
        val namedId = name?.let { names.findId(it) }?.takeIf { notationId !in heldIds }
        if (sectionIds == listOfNotNull(namedId)) return
        appendLine("$PROPERTY_MARK$SECTIONS: ${sectionIds.joinToString(NAME_SEPARATOR_TEXT) { names.nameOf(it) }}".trimEnd())
    }
    fun nameOf(notationId: NotationId) = track.notations.firstOrNull { it.notationId == notationId }?.name
        ?: track.arrangements.firstOrNull { it.notationId == notationId }?.name
        ?: UNNAMED_SECTION

    for (body in bodies.sortedBy { it.name != null }) {
        body.name?.let { appendLine("$it:") }
        body.lines.forEach { appendLine(it) }
        appendSections(body.notationId, body.name)
        appendLine()
    }
    for (arrangement in track.arrangements) {
        appendLine("${arrangement.name}: ${arrangement.notations.joinToString(NAME_SEPARATOR_TEXT) { nameOf(it) }}")
        appendSections(arrangement.notationId, arrangement.name)
        appendLine()
    }
}.trimEnd()

/** The lines written for a notation named [name], under its heading when it has one. */
internal data class NotationBody(
    val notationId: NotationId,
    val name: String?,
    val lines: List<String>,
)

internal fun NotationLines.toLines(): List<String> {
    val lines = measures.chunked(measuresPerLine.coerceAtLeast(1))
    if (lines.isEmpty()) return listOfNotNull(pickup?.let { "$it |" })
    return lines.mapIndexed { index, line ->
        val lead = pickup?.takeIf { index == 0 }?.let { "$it " }.orEmpty()
        "$lead| ${line.joinToString(" | ")} |"
    }
}

/** A notation named [name] written as the text of its [pickup], or `null` for none, and of each of its [measures]. */
internal data class NotationLines(
    val notationId: NotationId,
    val name: String?,
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

/** Reads a capo, a shift of up to 12 semitones up or down, or returns `null`. */
internal fun readCapo(text: String) = text.toIntOrNull()?.takeIf { it in -MAX_CAPO..MAX_CAPO }

/** The mark that leads the label of a property. */
private const val PROPERTY_MARK = "-"

private const val CAPO = "capo"
private const val SECTIONS = "sections"
private const val NAME_SEPARATOR = ','
private const val NAME_SEPARATOR_TEXT = ", "
private const val MAX_CAPO = 12

internal tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

private val WHITESPACE = Regex("\\s+")
