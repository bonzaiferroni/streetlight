package streetlight.web.model

import streetlight.model.data.Beats
import streetlight.model.data.Chord
import streetlight.model.data.ChordNote
import streetlight.model.data.ChordQuality
import streetlight.model.data.Groove
import streetlight.model.data.MelodyNote
import streetlight.model.data.Music
import streetlight.model.data.Phrase
import streetlight.model.data.Progression
import streetlight.model.data.Section
import streetlight.model.data.Seventh
import streetlight.model.data.Stanza
import streetlight.model.data.Track
import streetlight.model.data.lengthOf
import streetlight.model.data.sectionOrderOf
import streetlight.model.data.sectionsOf

/** The MIDI numbers of [midi] sounding together from [startTick] for [ticks]. */
class SongTone(
    val startTick: Int,
    val ticks: Int,
    val midi: List<Int>,
)

/** The [tones] of [track] in its song, in the order they start. */
data class TrackTones(
    val track: Track,
    val tones: List<SongTone>,
)

/**
 * The tones of [playing], tracks of a song of [tracks], laid along the arrangement of [music] with both capos applied.
 *
 * Each section lasts as long as the longest track that defines it, and a track rests through a section it lacks or runs
 * short of. A pickup starts before its section, so the first tones may start before tick 0.
 */
fun songTonesOf(music: Music, tracks: List<Track>, playing: List<Track>): List<TrackTones> {
    val order = tracks.sectionOrderOf(music)
    val onsets = order.runningFold(0) { onset, sectionId -> onset + (tracks.lengthOf(sectionId)?.ticks ?: 0) }
    return playing.map { track ->
        val capo = (music.capo ?: 0) + (track.capo ?: 0)
        val tones = order.zip(onsets).flatMap { (sectionId, onset) ->
            var tick = onset
            track.sectionsOf(sectionId).orEmpty().flatMap { section ->
                tonesOf(section, tick, capo).also { tick += section.length.ticks }
            }
        }
        TrackTones(track, tones)
    }
}

/** The tones of [section] placed at [onset] and raised [capo] semitones, its pickup sounding before it. */
private fun tonesOf(section: Section, onset: Int, capo: Int): List<SongTone> = when (section) {
    is Phrase -> {
        val midiOf = { note: MelodyNote -> note.pitch?.let { listOf(it.midi + capo) } }
        section.pickup.toTones(onset - section.pickup.sumOf { it.beats.ticks }, { it.beats }, midiOf) +
            section.notes.toTones(onset, { it.beats }, midiOf)
    }
    is Progression -> {
        val midiOf = { note: ChordNote -> note.chord?.toMidi()?.map { it + capo } }
        section.pickup.toTones(onset - section.pickup.sumOf { it.beats.ticks }, { it.beats }, midiOf) +
            section.notes.toTones(onset, { it.beats }, midiOf)
    }
    is Stanza, is Groove -> emptyList()
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

/** The MIDI numbers of the root, third, fifth and seventh of the chord, rooted in the octave below middle C. */
private fun Chord.toMidi(): List<Int> {
    val root = CHORD_ROOT_MIDI + root.value
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
