package streetlight.model.music.ui

import streetlight.model.music.Chord
import streetlight.model.music.Drum
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.LyricTrack
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.Stanza
import streetlight.model.music.LineVocal
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import streetlight.model.music.lyricLinesOfLegacy
import streetlight.model.music.nameOf
import streetlight.model.music.notationsOf
import streetlight.model.music.toMidi


/**
 * The layout of [notationId] in [track] against [music] within [window], or `null` when it has no length there.
 * Each of [lyrics] is a line of syllables sung to it, after the track's own stanza when it is a [LyricTrack].
 */
fun notationLayoutOf(
    track: Track,
    notationId: NotationId,
    music: Music,
    window: StaffWindow,
    lyrics: List<List<LineVocal>> = emptyList(),
): NotationLayout? {
    val ticks = track.lengthOf(notationId)?.ticks?.takeIf { it > 0 } ?: return null
    val sections = track.notationsOf(notationId).orEmpty()
    val notes = noteSourcesOf(sections, music.key).map { it.tick to noteLayoutOf(it, 0.0, window, music.key) } +
            drumNotesOf(sections, window)
    val measuresPerLine = sections.firstOrNull()?.measuresPerLine ?: DEFAULT_MEASURES_PER_LINE
    val isLyricTrack = track is LyricTrack
    val vocalLines = (if (isLyricTrack) listOf(syllablesOf(sections)) else emptyList()) + lyrics
    val name = track.nameOf(notationId)
    return notationLayoutOf(notationId, name, music, ticks, window, notes, vocalLines, measuresPerLine, !isLyricTrack)
}

/**
 * The layout of [notationId] shown as [name] against [music], [ticks] long within [window], laying each of [notes]
 * and the syllables of each of [vocalLines] by its tick into its measure, or before it for a pickup. Its text holds
 * [measuresPerLine] measures to a line, drawn on a staff when it [hasStaff].
 */
private fun notationLayoutOf(
    notationId: NotationId,
    name: String,
    music: Music,
    ticks: Int,
    window: StaffWindow,
    notes: List<Pair<Int, NoteLayout>>,
    vocalLines: List<List<LineVocal>>,
    measuresPerLine: Int,
    hasStaff: Boolean,
): NotationLayout {
    val measure = music.time.measure.ticks
    val count = (ticks + measure - 1) / measure
    val measures = List(count) { index ->
        val start = index * measure
        val length = minOf(measure, ticks - start)
        val measureNotes = notes.filter { (tick) -> tick in start until start + length }.map { (tick, note) ->
            note.copy(onset = (tick - start).toDouble() / length)
        }
        val measureVocals = vocalLines.map { line ->
            line.filter { it.tick in start until start + length }
                .map { VocalLayout((it.tick - start).toDouble() / length, it.text) }
        }
        MeasureLayout(length.toDouble() / ticks, measureNotes, measureVocals)
    }
    val pickup = notes.filter { (tick) -> tick < 0 }.map { (tick, note) -> note.copy(onset = tick.toDouble() / ticks) }
    val pickupVocals = vocalLines.map { line ->
        line.filter { it.tick < 0 }.map { VocalLayout(it.tick.toDouble() / ticks, it.text) }
    }
    return NotationLayout(
        notationId = notationId,
        name = name,
        ticks = ticks,
        steps = window.steps,
        staffStep = window.stepOf(STAFF_LOW),
        measures = measures,
        pickup = pickup,
        pickupVocals = pickupVocals,
        hasStaff = hasStaff,
        measuresPerLine = measuresPerLine,
        measureTicks = measure,
    )
}

fun notationLayoutsOfLegacy(
    track: Track,
    music: Music,
    tracks: List<Track> = listOf(track),
): List<NotationLayout> = track.notations.mapNotNull { notation ->
    val lyrics = vocalLinesOfLegacy(tracks, track, notation.notationId, music)
    val window = staffWindowOf(track, music.key, dragRoom = 0)
    notationLayoutOf(track, notation.notationId, music, window, lyrics)
}

/**
 * The window of [track] in [key]: the treble staff, stretched to reach every note of the track, with [dragRoom] steps
 * beyond at each end to drag a note past.
 */
fun staffWindowOf(track: Track, key: Key, dragRoom: Int = DRAG_ROOM_STEPS): StaffWindow {
    if (track is LyricTrack) return LYRIC_WINDOW
    val steps = track.notations.flatMap { drawnMidisOf(it, key) }.map { staffStepOf(it, key) }
    val low = minOf(steps.minOrNull() ?: STAFF_LOW, STAFF_LOW)
    val high = maxOf(steps.maxOrNull() ?: STAFF_HIGH, STAFF_HIGH)
    return StaffWindow(low - dragRoom, high + dragRoom)
}

fun vocalLinesOfLegacy(
    tracks: List<Track>,
    track: Track,
    notationId: NotationId,
    music: Music,
    play: Int? = null,
): List<List<LineVocal>> {
    val lines = tracks.lyricLinesOfLegacy(track, notationId, music)
    return if (play == null) lines.filter { it.isNotEmpty() }.distinct() else listOfNotNull(lines.getOrNull(play))
}

/** The layout of the note at [source], [onset] across what holds it, within [window]. */
fun noteLayoutOf(source: NoteSource, onset: Double, window: StaffWindow, key: Key): NoteLayout {
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
fun drawnMidisOf(chord: Chord, key: Key) = chord.toMidi(key).map { it + CHORD_DRAWN_SEMITONES }

/** The MIDI numbers of every note [section] draws in [key]. */
private fun drawnMidisOf(section: Notation, key: Key): List<Int> = when (section) {
    is Phrase -> (section.pickup + section.notes).mapNotNull { it.interval?.toMidi(key) }
    is Progression -> (section.pickup + section.notes).flatMap { note -> note.chord?.let { drawnMidisOf(it, key) }.orEmpty() }
    else -> emptyList()
}

/** The place of each note and chord of [sections], laid end to end, each pickup before its section. */
private fun noteSourcesOf(sections: List<Notation>, key: Key): List<NoteSource> {
    val sources = mutableListOf<NoteSource>()
    var onset = 0
    for (section in sections) {
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
                place(pickup, onset - pickup.sumOf { it.second }, true)
                place(section.notes.map { it.interval?.toMidi(key) to it.beats.ticks }, onset, false)
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
                place(pickup, onset - pickup.sumOf { it.second }, true)
                place(section.notes.map { it.chord to it.beats.ticks }, onset, false)
            }
            else -> Unit
        }
        onset += section.length.ticks
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

/** The tick of each note of the [Groove]s of [sections], laid end to end, paired with its notation within [window]. */
private fun drumNotesOf(sections: List<Notation>, window: StaffWindow): List<Pair<Int, NoteLayout>> {
    val notes = mutableListOf<Pair<Int, NoteLayout>>()
    var onset = 0
    for (section in sections) {
        if (section is Groove) notes += grooveNotesOf(section, window).map { (tick, note) -> onset + tick to note }
        onset += section.length.ticks
    }
    return notes
}

/** The tick of each note of [groove], paired with its notation within [window]: the hits of a tick as one note. */
private fun grooveNotesOf(groove: Groove, window: StaffWindow): List<Pair<Int, NoteLayout>> {
    return groove.lanes.flatMap { lane ->
        lane.beats.runningFold(0) { tick, beat -> tick + beat.beats.ticks }
            .zip(lane.beats)
            .filter { (_, beat) -> beat.hit != null }
            .map { (tick, _) -> tick to lane.drum }
    }.groupBy({ (tick) -> tick }, { (_, drum) -> drum }).map { (tick, drums) -> tick to drumLayoutOf(drums, window) }
}

/** The layout of [drums] struck together within [window], as heads alone. */
private fun drumLayoutOf(drums: List<Drum>, window: StaffWindow): NoteLayout {
    val staffSteps = drums.map { staffStepOf(it) }
    val lowest = staffSteps.min()
    return NoteLayout(
        onset = 0.0,
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

/**
 * The staff step of [midi] in [key], counted from C of octave 0. A note takes the line or space of its scale degree
 * from the major tonic, and a note between degrees shares the line of the degree below it.
 */
fun staffStepOf(midi: Int, key: Key): Int {
    val tonic = key.majorTonic.value
    val semitones = midi - tonic
    return semitones.floorDiv(12) * 7 + TONIC_STEPS[tonic] + DEGREE_STEPS[semitones.mod(12)]
}

/** The MIDI number of the degree of the scale of [key] on [staffStep], the inverse of [staffStepOf] for notes of the scale. */
fun midiOfStaffStep(staffStep: Int, key: Key): Int {
    val tonic = key.majorTonic.value
    val steps = staffStep - TONIC_STEPS[tonic]
    return tonic + steps.floorDiv(7) * 12 + SCALE_SEMITONES[steps.mod(7)]
}

/** The letter each major tonic is spelled on, counted from C, as flats name them in all but F#. */
private val TONIC_STEPS = listOf(0, 1, 1, 2, 2, 3, 3, 4, 5, 5, 6, 6)

/** The steps above the tonic of each semitone above it, the sharp of a degree sharing its line. */
private val DEGREE_STEPS = listOf(0, 0, 1, 1, 2, 3, 3, 4, 4, 5, 5, 6)

/** The semitones above the tonic of each degree of the scale. */
private val SCALE_SEMITONES = listOf(0, 2, 4, 5, 7, 9, 11)

/** The semitones above the tonic that fall between degrees of the scale. */
private val RAISED_SEMITONES = setOf(1, 3, 6, 8, 10)

/** The steps of room to drag a note past the highest and lowest of a track. */
private const val DRAG_ROOM_STEPS = 2

/** The semitones a chord is drawn above where it sounds. */
const val CHORD_DRAWN_SEMITONES = 12

/** The bottom line of the treble staff, E4, and its top line, F5. */
private const val STAFF_LOW = 37
private const val STAFF_HIGH = 45

/** The window of a lyric track, which has no staff: tall enough for a line of text. */
private val LYRIC_WINDOW = StaffWindow(0, 0)

/** The measures to a line of a section that gives none. */
private const val DEFAULT_MEASURES_PER_LINE = 4
