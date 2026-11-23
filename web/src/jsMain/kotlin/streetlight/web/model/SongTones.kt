package streetlight.web.model

import streetlight.model.music.Beats
import streetlight.model.music.ChordNote
import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import streetlight.model.music.Groove
import streetlight.model.music.Key
import streetlight.model.music.MelodyNote
import streetlight.model.music.Music
import streetlight.model.music.MusicSheet
import streetlight.model.music.Phrase
import streetlight.model.music.Progression
import streetlight.model.music.Notation
import streetlight.model.music.NotationId
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.music.Stanza
import streetlight.model.music.Track
import streetlight.model.music.lengthOf
import streetlight.model.music.notationsOf
import streetlight.model.music.repeatsOf
import streetlight.model.music.toMidi
import streetlight.model.music.trackOf


/** The seconds a tick of this music lasts at its tempo. */
val Music.secondsPerTick get() = 60.0 / bpm / time.pulse.ticks

/** The MIDI numbers of [midi] sounding together from [startTick] for [ticks]. */
class NoteTone(
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
    val tones: List<NoteTone>,
    val drums: List<DrumTone>,
) {
    /** Whether the track sounds at all. */
    val isSounding get() = tones.isNotEmpty() || drums.isNotEmpty()

    /** The tick of the first sound, or `null` when there is none. */
    val firstTick get() = (tones.map { it.startTick } + drums.map { it.startTick }).minOrNull()
}

/**
 * The tones of the tracks [trackIds] of this sheet, laid along its arrangement with both capos applied.
 *
 * Each section lasts its measures. A track repeats a shorter notation as many whole times as fit, and rests through a
 * section it does not play or what is left after its repeats. A pickup starts before its notation, so the first tones
 * may start before tick 0. A track or notation bound in [clips] sounds no tones, as its clip plays in its place.
 */
fun MusicSheet.musicTonesOf(
    trackIds: Collection<TrackId>,
    clips: Collection<TrackClip> = emptyList(),
): List<TrackTones> = tracks.filter { it.trackId in trackIds }.map { track ->
    val capo = (music.capo ?: 0) + (track.capo ?: 0)
    val tones = mutableListOf<NoteTone>()
    val drums = mutableListOf<DrumTone>()
    val trackClips = clips.filter { it.trackId == track.trackId }
    if (trackClips.none { it.notationId == null }) {
        val passes = notationPassesOf(music, track)
        // a clip of a notation or an arrangement silences every tone of the passes it spans
        val clipSpans = passes.filter { (notationId) -> trackClips.any { it.notationId == notationId } }
            .mapNotNull { (notationId, tick) -> track.lengthOf(notationId)?.let { tick until tick + it.ticks } }
        passes.forEach { (notationId, tick) ->
            if (clipSpans.any { tick in it }) return@forEach
            val notation = track.notations.firstOrNull { it.notationId == notationId } ?: return@forEach
            tones.addAll(tonesOf(notation, tick, music.key, capo))
            if (notation is Groove) drums.addAll(drumTonesOf(notation, tick))
        }
    }
    TrackTones(track, tones, drums.sortedBy { it.startTick })
}

/**
 * The tones of the notation or arrangement [notationId] of this sheet, the notations it holds laid end to end from
 * tick 0 with both capos applied. A notation bound in [clips] sounds no tones, as its clip plays in its place.
 */
fun MusicSheet.notationTonesOf(
    notationId: NotationId,
    clips: Collection<TrackClip> = emptyList(),
): TrackTones {
    val track = trackOf(notationId)
    val capo = (music.capo ?: 0) + (track.capo ?: 0)
    val clipIds = clips.filter { it.trackId == track.trackId }.mapNotNull { it.notationId }
    val tones = mutableListOf<NoteTone>()
    val drums = mutableListOf<DrumTone>()
    var onset = 0
    if (notationId !in clipIds) track.notationsOf(notationId).orEmpty().forEach { notation ->
        if (notation.notationId !in clipIds) {
            tones.addAll(tonesOf(notation, onset, music.key, capo))
            if (notation is Groove) drums.addAll(drumTonesOf(notation, onset))
        }
        onset += notation.length.ticks
    }
    return TrackTones(track, tones, drums.sortedBy { it.startTick })
}

/**
 * Each time [track] starts a notation or an arrangement along the arrangement of [music], each of its repeats
 * included, paired with the tick it starts at. An arrangement is followed by the notations it holds.
 */
fun notationPassesOf(music: Music, track: Track): List<Pair<NotationId, Int>> {
    val passes = mutableListOf<Pair<NotationId, Int>>()
    // returns the length of what it placed
    fun place(notationId: NotationId, tick: Int): Int {
        passes.add(notationId to tick)
        track.notations.firstOrNull { it.notationId == notationId }?.let { return it.length.ticks }
        val arrangement = track.arrangements.firstOrNull { it.notationId == notationId } ?: return 0
        return arrangement.notations.fold(0) { length, heldId -> length + place(heldId, tick + length) }
    }

    var onset = 0
    music.arrangement.forEach { sectionId ->
        val span = music.lengthOf(sectionId) ?: Beats(0)
        val notationId = track.sections[sectionId]
        // a length means the notation resolves, so its arrangements hold no cycle
        notationId?.let { track.lengthOf(it) }?.let { length ->
            repeat(track.repeatsOf(notationId, span)) { place(notationId, onset + it * length.ticks) }
        }
        onset += span.ticks
    }
    return passes
}

/** The tick at which [track] starts [notationId] each time it plays it along the arrangement of [music]. */
fun notationStartsOf(music: Music, track: Track, notationId: NotationId) =
    notationPassesOf(music, track).filter { it.first == notationId }.map { it.second }

/**
 * The first pitched tone of [notationId] in [track], its pickup's first when it has one, with both capos of [music]
 * applied, or `null` when it sounds no pitch.
 */
fun firstToneOf(music: Music, track: Track, notationId: NotationId): NoteTone? {
    val capo = (music.capo ?: 0) + (track.capo ?: 0)
    var onset = 0
    return track.notationsOf(notationId).orEmpty().flatMap { notation ->
        tonesOf(notation, onset, music.key, capo).also { onset += notation.length.ticks }
    }.minByOrNull { it.startTick }
}

/** The tones of [section] in [key] placed at [onset] and raised [capo] semitones, its pickup sounding before it. */
private fun tonesOf(section: Notation, onset: Int, key: Key, capo: Int): List<NoteTone> = when (section) {
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
private fun <N> List<N>.toTones(onset: Int, beatsOf: (N) -> Beats, midiOf: (N) -> List<Int>?): List<NoteTone> {
    var tick = onset
    return mapNotNull { note ->
        val ticks = beatsOf(note).ticks
        val tone = midiOf(note)?.let { NoteTone(tick, ticks, it) }
        tick += ticks
        tone
    }
}
