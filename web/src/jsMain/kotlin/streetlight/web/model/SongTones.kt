package streetlight.web.model

import streetlight.model.music.Beats
import streetlight.model.music.ChordNote
import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MelodyNote
import streetlight.model.music.Music
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Section
import streetlight.model.music.SectionId
import streetlight.model.music.TrackSectionId
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.sectionOrderOf
import streetlight.model.music.sectionsOf
import streetlight.model.music.toMidi


/** The seconds a tick of this music lasts at its tempo. */
val Music.secondsPerTick get() = 60.0 / bpm / time.pulse.ticks

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
 * section, so the first tones may start before tick 0. A section of a track in [taken] sounds no tones, as a take
 * plays in its place.
 */
fun songTonesOf(
    music: Music,
    tracks: List<Track>,
    playing: List<Track>,
    taken: Set<TrackSectionId> = emptySet(),
): List<TrackTones> {
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
                // an arrangement expands into its sections, and a take may stand for either
                val isTaken = TrackSectionId(track.trackId, sectionId) in taken ||
                    TrackSectionId(track.trackId, section.sectionId) in taken
                if (!isTaken) {
                    tones.addAll(tonesOf(section, tick, music.key, capo))
                    if (section is Groove) drums.addAll(drumTonesOf(section, tick))
                }
                tick += section.length.ticks
            }
        }
        TrackTones(track, tones, drums.sortedBy { it.startTick })
    }
}

/**
 * The tick at which [track], one of [tracks], starts [sectionId] each time it plays it along the arrangement of
 * [music], each of its repeats included, whether it is played by name or within an arrangement.
 */
fun sectionStartsOf(music: Music, tracks: List<Track>, track: Track, sectionId: SectionId): List<Int> {
    val order = tracks.sectionOrderOf(music)
    val onsets = order.runningFold(0) { onset, id -> onset + (tracks.lengthOf(id)?.ticks ?: 0) }
    return order.zip(onsets).flatMap { (orderId, onset) ->
        val sections = track.sectionsOf(orderId).orEmpty()
        val span = tracks.lengthOf(orderId) ?: Beats(0)
        var tick = onset
        buildList {
            repeat(track.repeatsOf(orderId, span)) {
                if (orderId == sectionId) add(tick)
                sections.forEach { section ->
                    if (orderId != sectionId && section.sectionId == sectionId) add(tick)
                    tick += section.length.ticks
                }
            }
        }
    }
}

/** A section of a track. */


/**
 * The first pitched tone of [sectionId] in [track], its pickup's first when it has one, with both capos of [music]
 * applied, or `null` when the section sounds no pitch.
 */
fun firstToneOf(music: Music, track: Track, sectionId: SectionId): SongTone? {
    val capo = (music.capo ?: 0) + (track.capo ?: 0)
    var onset = 0
    return track.sectionsOf(sectionId).orEmpty().flatMap { section ->
        tonesOf(section, onset, music.key, capo).also { onset += section.length.ticks }
    }.minByOrNull { it.startTick }
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
