package streetlight.model.music.ui

import streetlight.model.music.Chord
import streetlight.model.music.Drum
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.LineVocal
import streetlight.model.music.LyricTrack
import streetlight.model.music.MusicSheet
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Stanza
import streetlight.model.music.TimeSignature
import streetlight.model.music.Track
import streetlight.model.music.TrackId
import streetlight.model.music.lengthOf
import streetlight.model.music.lyricLinesOf
import streetlight.model.music.nameOf
import streetlight.model.music.notationsOf
import streetlight.model.music.toMidi

/**
 * The layout of [notationId], or `null` when it has no length, with each line sung to it, or only the line of the pass
 * at [passIndex] when one is given.
 */
fun MusicSheet.notationLayoutOf(notationId: NotationId, passIndex: Int? = null): NotationLayout? {
    val track = trackHolding(notationId) ?: return null
    val ticks = track.lengthOf(notationId)?.ticks?.takeIf { it > 0 } ?: return null
    val sections = track.notationsOf(notationId).orEmpty()
    val window = staffWindowOf(track.trackId)
    val isLyricTrack = track is LyricTrack
    val vocalLines = if (isLyricTrack) listOf(syllablesOf(sections)) else vocalLinesOf(notationId, passIndex)

    return NotationLayout(
        notationId = notationId,
        name = track.nameOf(notationId),
        ticks = ticks,
        steps = window.steps,
        staffStep = window.stepOf(STAFF_LOW),
        measures = measureLayoutsOf(sections, window, music.key, music.time, vocalLines),
        hasStaff = !isLyricTrack,
        lineTicks = sections.maxOf { it.measuresPerLine } * music.time.measure.ticks,
    )
}

/**
 * The measures of [sections] laid end to end in measures of [time], within [window] in [key], with the syllables of
 * each of [vocalLines]. Each section's measures start at its own start and break into lines of its measures per line,
 * and what sounds before the first section makes up a pickup.
 */
private fun measureLayoutsOf(
    sections: List<Notation>,
    window: StaffWindow,
    key: Key,
    time: TimeSignature,
    vocalLines: List<List<LineVocal>>,
): List<MeasureLayout> {
    val measureTicks = time.measure.ticks
    val starts = sections.runningFold(0) { tick, section -> tick + section.length.ticks }
    val ticks = starts.last()

    // lays the measure [length] ticks from [start], with the notes of the sections at [reach]
    fun measureLayoutOf(number: Int, line: Int, start: Int, length: Int, reach: List<Int>): MeasureLayout {
        val notes = reach.flatMap { index ->
            sections.getOrNull(index)?.let { notesLayoutOf(it, starts[index], start, length, window, key) }.orEmpty()
        }
        val vocals = vocalLines.map { vocalLine ->
            vocalLine.filter { it.tick in start until start + length }
                .map { VocalLayout((it.tick - start).toDouble() / length, it.text) }
        }
        return MeasureLayout(number, line, length.toDouble() / ticks, notes, vocals)
    }

    val measures = mutableListOf<MeasureLayout>()
    val sungPickupTicks = -(vocalLines.flatten().minOfOrNull { it.tick } ?: 0)
    val pickupTicks = maxOf(sections.firstOrNull()?.let { pickupTicksOf(it) } ?: 0, sungPickupTicks)
    if (pickupTicks > 0) measures += measureLayoutOf(0, 0, -pickupTicks, pickupTicks, listOf(0))

    var number = 0
    var line = 0
    sections.forEachIndexed { index, section ->
        val count = (section.length.ticks + measureTicks - 1) / measureTicks
        val perLine = section.measuresPerLine.coerceAtLeast(1)
        repeat(count) { measureIndex ->
            val start = starts[index] + measureIndex * measureTicks
            val length = minOf(measureTicks, starts[index + 1] - start)
            measures += measureLayoutOf(++number, line + measureIndex / perLine, start, length, listOf(index, index + 1))
        }
        line += (count + perLine - 1) / perLine
    }
    return measures
}

/**
 * The layouts of the notes of [section], placed from [sectionStart] within [window] in [key], that start in the measure
 * [length] ticks from [start], each at its onset across it.
 */
private fun notesLayoutOf(
    section: Notation,
    sectionStart: Int,
    start: Int,
    length: Int,
    window: StaffWindow,
    key: Key,
): List<NoteLayout> {
    fun isWithin(tick: Int) = tick in start until start + length
    fun onsetOf(tick: Int) = (tick - start).toDouble() / length
    return when (section) {
        is Phrase, is Progression -> noteSourcesOf(section, sectionStart, key).filter { isWithin(it.tick) }
            .map { noteLayoutOf(it, onsetOf(it.tick), window, key) }
        is Groove -> drumHitsOf(section, sectionStart).filterKeys { isWithin(it) }
            .map { (tick, drums) -> drumLayoutOf(drums, onsetOf(tick), window) }
        is Stanza -> emptyList()
    }
}

fun MusicSheet.staffWindowOf(trackId: TrackId, dragRoom: Int = DRAG_ROOM_STEPS): StaffWindow {
    val track = tracks.first { it.trackId == trackId }
    if (track is LyricTrack) return LYRIC_WINDOW
    val steps = track.notations.flatMap { drawnMidisOf(it, music.key) }.map { staffStepOf(it, music.key) }
    val low = minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW)
    val high = maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH)
    return StaffWindow(low - dragRoom, high + dragRoom)
}

/** The lines sung to [notationId]: each different one, or only the line of the pass at [passIndex] when one is given. */
private fun MusicSheet.vocalLinesOf(notationId: NotationId, passIndex: Int?): List<List<LineVocal>> {
    val lines = lyricLinesOf(notationId, this)
    return if (passIndex == null) lines.filter { it.isNotEmpty() }.distinct() else listOfNotNull(lines.getOrNull(passIndex))
}

private fun MusicSheet.trackHolding(notationId: NotationId): Track? = tracks.firstOrNull { track ->
    track.notations.any { it.notationId == notationId } || track.arrangements.any { it.notationId == notationId }
}

/** The ticks of the pickup of [section]. */
private fun pickupTicksOf(section: Notation) = when (section) {
    is Phrase -> section.pickup.sumOf { it.beats.ticks }
    is Progression -> section.pickup.sumOf { it.beats.ticks }
    is Stanza -> section.pickup.sumOf { it.beats.ticks }
    is Groove -> 0
}

/** The layout of the note at [source], [onset] across its measure, within [window]. */
private fun noteLayoutOf(source: NoteSource, onset: Double, window: StaffWindow, key: Key): NoteLayout {
    val (value, isDotted) = noteValueOf(source.ticks)
    val staffSteps = (source.chord?.let { drawnMidisOf(it, key) } ?: listOf(source.midi)).map { staffStepOf(it, key) }
    val lowest = staffSteps.min()
    val isRaised = (source.midi - key.majorTonic.value).mod(12) in RAISED_SEMITONES
    return NoteLayout(
        onset = onset,
        step = window.stepOf(lowest),
        value = value,
        isDotted = isDotted,
        heads = staffSteps.map { NoteHead(it - lowest, HeadShape.Round) },
        ledgers = ledgersOf(lowest, staffSteps.max()).map { it - lowest },
        isRaised = isRaised,
        source = source,
    )
}

/** The MIDI numbers [chord] is drawn at in [key], an octave above where it sounds, so it sits on the treble staff. */
private fun drawnMidisOf(chord: Chord, key: Key) = chord.toMidi(key).map { it + CHORD_DRAWN_SEMITONES }

/** The MIDI numbers of every note [section] draws in [key]. */
private fun drawnMidisOf(section: Notation, key: Key): List<Int> = when (section) {
    is Phrase -> (section.pickup + section.notes).mapNotNull { it.interval?.toMidi(key) }
    is Progression -> (section.pickup + section.notes).flatMap { note -> note.chord?.let { drawnMidisOf(it, key) }.orEmpty() }
    else -> emptyList()
}

/** The place of each note and chord of [section] in [key], from [sectionStart], its pickup before it. */
private fun noteSourcesOf(section: Notation, sectionStart: Int, key: Key): List<NoteSource> {
    val sources = mutableListOf<NoteSource>()
    when (section) {
        is Phrase -> {
            fun place(notes: List<Pair<Int?, Int>>, start: Int, isPickup: Boolean) {
                var tick = start
                notes.forEachIndexed { index, (midi, ticks) ->
                    if (midi != null) sources.add(NoteSource(section.notationId, index, isPickup, tick, ticks, midi))
                    tick += ticks
                }
            }
            val pickup = section.pickup.map { it.interval?.toMidi(key) to it.beats.ticks }
            place(pickup, sectionStart - pickup.sumOf { it.second }, true)
            place(section.notes.map { it.interval?.toMidi(key) to it.beats.ticks }, sectionStart, false)
        }
        is Progression -> {
            fun place(notes: List<Pair<Chord?, Int>>, start: Int, isPickup: Boolean) {
                var tick = start
                notes.forEachIndexed { index, (chord, ticks) ->
                    if (chord != null) {
                        val midi = drawnMidisOf(chord, key).first()
                        sources.add(NoteSource(section.notationId, index, isPickup, tick, ticks, midi, chord))
                    }
                    tick += ticks
                }
            }
            val pickup = section.pickup.map { it.chord to it.beats.ticks }
            place(pickup, sectionStart - pickup.sumOf { it.second }, true)
            place(section.notes.map { it.chord to it.beats.ticks }, sectionStart, false)
        }
        else -> Unit
    }
    return sources
}

/** The syllables of the [Stanza]s of [sections], laid end to end, each pickup before its section. */
private fun syllablesOf(sections: List<Notation>): List<LineVocal> {
    val syllables = mutableListOf<LineVocal>()
    var onset = 0
    for (section in sections) {
        if (section is Stanza) {
            var tick = onset - section.pickup.sumOf { it.beats.ticks }
            for (note in section.pickup + section.notes) {
                note.text?.let { syllables.add(LineVocal(tick, it)) }
                tick += note.beats.ticks
            }
        }
        onset += section.length.ticks
    }
    return syllables
}

/** The drums [groove] strikes at each tick it strikes any, from [sectionStart]. */
private fun drumHitsOf(groove: Groove, sectionStart: Int): Map<Int, List<Drum>> {
    return groove.lanes.flatMap { lane ->
        lane.beats.runningFold(sectionStart) { tick, beat -> tick + beat.beats.ticks }
            .zip(lane.beats)
            .filter { (_, beat) -> beat.hit != null }
            .map { (tick, _) -> tick to lane.drum }
    }.groupBy({ (tick) -> tick }, { (_, drum) -> drum })
}

/** The layout of [drums] struck together [onset] across their measure, within [window], as heads alone. */
private fun drumLayoutOf(drums: List<Drum>, onset: Double, window: StaffWindow): NoteLayout {
    val staffSteps = drums.map { staffStepOf(it) }
    val lowest = staffSteps.min()
    return NoteLayout(
        onset = onset,
        step = window.stepOf(lowest),
        value = null,
        isDotted = false,
        heads = drums.map { NoteHead(staffStepOf(it) - lowest, headShapeOf(it)) },
        ledgers = ledgersOf(lowest, staffSteps.max()).map { it - lowest },
        isRaised = false,
        source = null,
    )
}

/** The staff step [drum] is written on, counted from C of octave 0. */
private fun staffStepOf(drum: Drum) = when (drum) {
    Drum.Kick -> 38
    Drum.Snare -> 42
    Drum.Tom -> 44
    Drum.Ride -> 45
    Drum.HiHat -> 46
    Drum.Crash -> 47
}

/** The head [drum] is written with. */
private fun headShapeOf(drum: Drum) = when (drum) {
    Drum.Kick, Drum.Snare, Drum.Tom -> HeadShape.Round
    Drum.HiHat, Drum.Crash, Drum.Ride -> HeadShape.Cross
}

/** The ledger lines a note reaching from [lowest] to [highest] wants, one on each line between it and the staff. */
private fun ledgersOf(lowest: Int, highest: Int) =
    (STAFF_HIGH + 2..highest step 2).toList() + (STAFF_LOW - 2 downTo lowest step 2).toList()

/** The value written for [ticks], the longest that fits, and whether it wants a dot for the half more. */
private fun noteValueOf(ticks: Int): Pair<NoteValue, Boolean> {
    val value = NoteValue.entries.firstOrNull { it.ticks <= ticks } ?: NoteValue.Sixteenth
    return value to (ticks * 2 >= value.ticks * 3)
}

/** The steps of room to drag a note past the highest and lowest of a track. */
private const val DRAG_ROOM_STEPS = 2

/** The bottom line of the treble staff, E4, and its top line, F5. */
private const val STAFF_LOW = 37
private const val STAFF_HIGH = 45

/** The window of a lyric track, which has no staff: tall enough for a line of text. */
private val LYRIC_WINDOW = StaffWindow(0, 0)

/** The semitones above the tonic that fall between degrees of the scale. */
private val RAISED_SEMITONES = setOf(1, 3, 6, 8, 10)

/** The semitones a chord is drawn above where it sounds. */
private const val CHORD_DRAWN_SEMITONES = 12
