package streetlight.model.music.text

import streetlight.model.music.Beats
import streetlight.model.music.Drum
import streetlight.model.music.DrumBeat
import streetlight.model.music.DrumHit
import streetlight.model.music.DrumLane
import streetlight.model.music.DrumTrack
import streetlight.model.music.Groove
import streetlight.model.music.SectionId
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackId

/**
 * The parser of a drum track's tab.
 *
 * Each line of a section is a row: the label of a drum, as `HH`, `SD` or `BD`, then its measures between bar lines.
 * Each character of a measure is an even slot of it: `x` or `o` a hit, `X` or `O` an accent, `g` a ghost, and `-` the
 * hit before ringing on, or a rest before the first. Rows that follow one another are a system and sound together. A
 * row naming a drum the system already holds starts the next, so a long groove wraps.
 */
object DrumParser {
    /** Reads the drum track [trackId] from [text], with measures cut by [time] and sections found in [names]. */
    fun read(trackId: TrackId, text: String, time: TimeSignature, names: SectionNames): NotationRead<DrumTrack> {
        val errors = mutableListOf<NotationError>()
        val trackText = readTrackText(text, errors, names)
        val grooves = trackText.sections.map { section -> readGroove(section, time, errors) }
        return NotationRead(DrumTrack(trackId, grooves, trackText.arrangements, trackText.capo), errors)
    }

    /** Reads the section [sectionId] from [text], the rows of its tab alone, cut by [time]. */
    fun readSection(sectionId: SectionId, text: String, time: TimeSignature): NotationRead<Groove> {
        val errors = mutableListOf<NotationError>()
        return NotationRead(readGroove(sectionTextOf(sectionId, text, errors), time, errors), errors)
    }

    /** Writes [track] as the tab [read] reads, with measures cut by [time] and sections named by [names]. */
    fun write(track: DrumTrack, time: TimeSignature, names: SectionNames): String {
        val bodies = track.sections.map { SectionBody(it.sectionId, writeGroove(it, time.measure)) }
        return writeTrackBodies(bodies, track.arrangements, track.capo, names)
    }

    /** Writes [groove] as the tab [readSection] reads, with measures cut by [time]. */
    fun writeSection(groove: Groove, time: TimeSignature) = writeGroove(groove, time.measure).joinToString("\n")

    private fun readGroove(section: SectionText, time: TimeSignature, errors: MutableList<NotationError>): Groove {
        val lanes = linkedMapOf<Drum, MutableList<DrumBeat>>()
        val system = mutableSetOf<Drum>()
        var systemStart = 0
        var firstMeasure: String? = null
        var firstLineMeasures: Int? = null

        // a system ends where its longest row does, and every lane rests to there before the next begins
        fun closeSystem() {
            val end = lanes.values.maxOfOrNull { it.ticks() } ?: systemStart
            lanes.values.forEach { laneBeats -> laneBeats.restUntil(end) }
            system.clear()
            systemStart = end
        }

        for (line in section.lines) {
            fun error(message: String) = errors.add(NotationError(line.number, message))
            val barLine = line.toBarLine()
            val label = barLine.lead
            val drum = label?.let { readDrum(it) }
            if (drum == null) {
                error(if (label == null) "A row without a drum" else "Unknown drum $label")
                continue
            }
            if (drum in system) closeSystem()
            system.add(drum)
            if (firstMeasure == null) firstMeasure = barLine.measures.firstOrNull()
            if (firstLineMeasures == null) firstLineMeasures = barLine.measures.size

            val laneBeats = lanes.getOrPut(drum) { mutableListOf() }
            laneBeats.restUntil(systemStart)
            for (measure in barLine.measures) {
                val slots = measure.filterNot { it.isWhitespace() }
                if (slots.isEmpty()) {
                    error("An empty measure")
                    continue
                }
                slots.toList().zip(slotBeatsOf(slots.length, time.measure)).forEach { (slot, beats) ->
                    when (slot) {
                        RING -> laneBeats.extendOrRest(beats)
                        else -> {
                            val hit = readHit(slot)
                            if (hit == null) {
                                error("Unknown hit $slot")
                                laneBeats.extendOrRest(beats)
                            } else {
                                laneBeats.add(DrumBeat(hit, beats))
                            }
                        }
                    }
                }
            }
        }
        closeSystem()

        return Groove(
            sectionId = section.sectionId,
            lanes = lanes.map { (drum, laneBeats) -> DrumLane(drum, laneBeats) },
            slotsPerMeasure = firstMeasure?.count { !it.isWhitespace() }?.takeIf { it > 0 } ?: SLOTS_PER_MEASURE,
            measuresPerLine = firstLineMeasures?.takeIf { it > 0 } ?: MEASURES_PER_LINE,
        )
    }

    /**
     * Writes the rows of [groove], [Groove.measuresPerLine] measures to a system, each measure cut into
     * [Groove.slotsPerMeasure] slots, or finer where a hit falls between them.
     */
    private fun writeGroove(groove: Groove, measure: Beats): List<String> {
        val measureTicks = measure.ticks
        val measureCount = (groove.length.ticks + measureTicks - 1) / measureTicks
        val onsets = groove.lanes.associate { lane ->
            lane.drum to lane.beats.runningFold(0) { onset, note -> onset + note.beats.ticks }
                .zip(lane.beats) { onset, note -> onset to note }
                .filter { (_, note) -> note.hit != null }
                .associate { (onset, note) -> onset to note.hit!! }
        }
        val baseSlot = measureTicks / groove.slotsPerMeasure.coerceAtLeast(1)
        val measures = (0 until measureCount).map { index ->
            val start = index * measureTicks
            val offsets = onsets.values.flatMap { it.keys }.filter { it in start until start + measureTicks }
            val slot = offsets.fold(baseSlot) { divisor, onset -> gcd(divisor, onset - start) }
            groove.lanes.associate { lane ->
                lane.drum to (0 until measureTicks / slot).joinToString("") { slotIndex ->
                    val hit = onsets.getValue(lane.drum)[start + slotIndex * slot]
                    hit?.let { writeHit(it, lane.drum) } ?: RING.toString()
                }
            }
        }
        val labelWidth = groove.lanes.maxOfOrNull { labelOf(it.drum).length } ?: 0
        return measures.chunked(groove.measuresPerLine.coerceAtLeast(1)).flatMapIndexed { index, system ->
            val rows = groove.lanes.map { lane ->
                labelOf(lane.drum).padEnd(labelWidth) + "|" + system.joinToString("|") { it.getValue(lane.drum) } + "|"
            }
            // a blank line between systems keeps a wrapped groove readable
            if (index == 0) rows else listOf("") + rows
        }
    }

    private fun readDrum(label: String) = LABELS.entries.firstOrNull { (_, labels) ->
        labels.any { it.equals(label, ignoreCase = true) }
    }?.key

    private fun labelOf(drum: Drum) = LABELS.getValue(drum).first()

    private fun readHit(slot: Char) = when (slot) {
        'x', 'o' -> DrumHit.Normal
        'X', 'O' -> DrumHit.Accent
        'g' -> DrumHit.Ghost
        else -> null
    }

    private fun writeHit(hit: DrumHit, drum: Drum) = when (hit) {
        DrumHit.Normal -> if (drum in CYMBALS) 'x' else 'o'
        DrumHit.Accent -> if (drum in CYMBALS) 'X' else 'O'
        DrumHit.Ghost -> 'g'
    }.toString()

    private fun List<DrumBeat>.ticks() = sumOf { it.beats.ticks }

    /** Extends the last note by [beats], or adds a rest of [beats] when there is none. */
    private fun MutableList<DrumBeat>.extendOrRest(beats: Beats) {
        val last = lastOrNull()
        if (last == null) add(DrumBeat(null, beats))
        else this[lastIndex] = last.copy(beats = last.beats + beats)
    }

    /** Extends the lane to [tick] with the note it ends on ringing, or with a rest when it is empty. */
    private fun MutableList<DrumBeat>.restUntil(tick: Int) {
        val gap = tick - ticks()
        if (gap > 0) extendOrRest(Beats(gap))
    }
}

/** The labels a row of each drum may carry, the first being the one written. */
private val LABELS = linkedMapOf(
    Drum.HiHat to listOf("HH"),
    Drum.Crash to listOf("CR", "CC"),
    Drum.Ride to listOf("RD", "R"),
    Drum.Snare to listOf("SD", "S"),
    Drum.Tom to listOf("T", "HT", "MT", "FT", "LT"),
    Drum.Kick to listOf("BD", "B", "K"),
)

private val CYMBALS = setOf(Drum.HiHat, Drum.Crash, Drum.Ride)
private const val RING = '-'
private const val SLOTS_PER_MEASURE = 16
private const val MEASURES_PER_LINE = 2
