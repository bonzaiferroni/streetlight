package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.Chord
import streetlight.model.data.ChordNote
import streetlight.model.data.ChordQuality
import streetlight.model.data.Drum
import streetlight.model.data.DrumHit
import streetlight.model.data.Groove
import streetlight.model.data.Key
import streetlight.model.data.MelodyNote
import streetlight.model.data.Music
import streetlight.model.data.Phrase
import streetlight.model.data.Progression
import streetlight.model.data.Section
import streetlight.model.data.Seventh
import streetlight.model.data.Stanza
import streetlight.model.data.Track
import streetlight.model.data.lengthOf
import streetlight.model.data.repeatsOf
import streetlight.model.data.sectionOrderOf
import streetlight.model.data.sectionsOf

/** The seconds a tick of this music lasts at its tempo. */
val Music.secondsPerTick get() = 60.0 / bpm / time.beat.ticks

/** The MIDI numbers of [midi] sounding together from [startTick] for [ticks]. */
class SongTone(
    val startTick: Int,
    val ticks: Int,
    val midi: List<Int>,
)

/** A [hit] of [drum] struck at [startTick]. */
class DrumTone(
    val startTick: Int,
    val drum: Drum,
    val hit: DrumHit,
)

/** The pitched [tones] and the [drums] of [track] in its song, each in the order they start. */
data class TrackTones(
    val track: Track,
    val tones: List<SongTone>,
    val drums: List<DrumTone>,
) {
    /** Whether the track sounds at all. */
    val isSounding get() = tones.isNotEmpty() || drums.isNotEmpty()

    /** The tick of the first sound, or `null` when there is none. */
    val firstTick get() = (tones.map { it.startTick } + drums.map { it.startTick }).minOrNull()
}

/**
 * The tones of [playing], tracks of a song of [tracks], laid along the arrangement of [music] with both capos applied.
 *
 * Each section lasts as long as the longest track that defines it. A track repeats a shorter section as many whole
 * times as fit, and rests through a section it lacks or what is left after its repeats. A pickup starts before its
 * section, so the first tones may start before tick 0.
 */
fun songTonesOf(music: Music, tracks: List<Track>, playing: List<Track>): List<TrackTones> {
    val order = tracks.sectionOrderOf(music)
    val onsets = order.runningFold(0) { onset, sectionId -> onset + (tracks.lengthOf(sectionId)?.ticks ?: 0) }
    return playing.map { track ->
        val capo = (music.capo ?: 0) + (track.capo ?: 0)
        val tones = mutableListOf<SongTone>()
        val drums = mutableListOf<DrumTone>()
        order.zip(onsets).forEach { (sectionId, onset) ->
            var tick = onset
            val sections = track.sectionsOf(sectionId).orEmpty()
            val span = tracks.lengthOf(sectionId) ?: Beats(0)
            List(track.repeatsOf(sectionId, span)) { sections }.flatten().forEach { section ->
                tones.addAll(tonesOf(section, tick, music.key, capo))
                if (section is Groove) drums.addAll(drumTonesOf(section, tick))
                tick += section.length.ticks
            }
        }
        TrackTones(track, tones, drums.sortedBy { it.startTick })
    }
}

/** The tones of [section] in [key] placed at [onset] and raised [capo] semitones, its pickup sounding before it. */
private fun tonesOf(section: Section, onset: Int, key: Key, capo: Int): List<SongTone> = when (section) {
    is Phrase -> {
        val midiOf = { note: MelodyNote -> note.interval?.let { listOf(it.toMidi(key) + capo) } }
        section.pickup.toTones(onset - section.pickup.sumOf { it.beats.ticks }, { it.beats }, midiOf) +
            section.notes.toTones(onset, { it.beats }, midiOf)
    }
    is Progression -> {
        val midiOf = { note: ChordNote -> note.chord?.toMidi(key)?.map { it + capo } }
        section.pickup.toTones(onset - section.pickup.sumOf { it.beats.ticks }, { it.beats }, midiOf) +
            section.notes.toTones(onset, { it.beats }, midiOf)
    }
    is Stanza, is Groove -> emptyList()
}

/** The hits of every lane of [groove] placed at [onset]. */
private fun drumTonesOf(groove: Groove, onset: Int) = groove.lanes.flatMap { lane ->
    var tick = onset
    lane.beats.mapNotNull { beat ->
        val tone = beat.hit?.let { DrumTone(tick, lane.drum, it) }
        tick += beat.beats.ticks
        tone
    }
}

/** The notes laid end to end from [onset], each a tone of the MIDI numbers [midiOf] gives, or a rest for `null`. */
private fun <N> List<N>.toTones(onset: Int, beatsOf: (N) -> Beats, midiOf: (N) -> List<Int>?): List<SongTone> {
    var tick = onset
    return mapNotNull { note ->
        val ticks = beatsOf(note).ticks
        val tone = midiOf(note)?.let { SongTone(tick, ticks, it) }
        tick += ticks
        tone
    }
}

/** The MIDI numbers of the root, third, fifth and seventh of the chord in [key], rooted below middle C. */
fun Chord.toMidi(key: Key): List<Int> {
    val root = CHORD_ROOT_MIDI + (key.tonic.value + root.value).mod(12)
    val third = when (quality) {
        ChordQuality.Minor, ChordQuality.Diminished -> 3
        ChordQuality.Major, ChordQuality.Augmented -> 4
        ChordQuality.Sus2 -> 2
        ChordQuality.Sus4 -> 5
    }
    val fifth = when (quality) {
        ChordQuality.Diminished -> 6
        ChordQuality.Augmented -> 8
        else -> 7
    }
    val seventh = when (seventh) {
        Seventh.Minor -> 10
        Seventh.Major -> 11
        Seventh.Diminished -> 9
        null -> null
    }
    return listOfNotNull(0, third, fifth, seventh).map { root + it }
}

private const val CHORD_ROOT_MIDI = 48
