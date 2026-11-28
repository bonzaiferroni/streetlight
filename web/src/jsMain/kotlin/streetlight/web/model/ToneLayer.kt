package streetlight.web.model

import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import streetlight.model.music.Key
import streetlight.model.music.Music
import streetlight.model.music.TimeSignature
import streetlight.model.music.TrackClip
import streetlight.model.music.TrackId
import streetlight.model.music.toMidi
import streetlight.model.music.ui.ChordEvent
import streetlight.model.music.ui.DrumEvent
import streetlight.model.music.ui.MusicSource
import streetlight.model.music.ui.NotationEvent
import streetlight.model.music.ui.NotationPass
import streetlight.model.music.ui.NoteEvent
import streetlight.model.music.ui.VocalEvent

/** The [tones] of a layer of sound, in the order they start. */
data class ToneLayer(
    val tones: List<Tone>,
) {
    /** Whether the layer sounds at all. */
    val isNotEmpty get() = tones.isNotEmpty()

    /** The tick of the first sound, or `null` when there is none. */
    val firstTick get() = tones.minOfOrNull { it.startTick }
}

/** The seconds a tick of this music lasts at its tempo. */
val Music.secondsPerTick get() = 60.0 / bpm / time.pulse.ticks

/** A sound of a [ToneLayer], from [startTick] to [endTick] at [volume], an amplitude from 0 to 1. */
sealed interface Tone {
    val startTick: Int
    val endTick: Int
    val volume: Double
}

/** The MIDI numbers of [midi] sounding together from [startTick] for [ticks]. */
class NoteTone(
    override val startTick: Int,
    val ticks: Int,
    val midi: List<Int>,
    override val volume: Double,
) : Tone {
    override val endTick get() = startTick + ticks
}

/** [drum] struck at [startTick], a sound with no length. */
class DrumTone(
    override val startTick: Int,
    val drum: Drum,
    override val volume: Double,
) : Tone {
    override val endTick get() = startTick
}

/** The volume a hit of a drum sounds at. */
val DrumHit.volume get() = when (this) {
    DrumHit.Accent -> 1.0
    DrumHit.Normal -> 1.0 / 1.3
    DrumHit.Ghost -> 0.4 / 1.3
}

/** [midis] held a measure of [time], three measures before the downbeat, to give the pitch to start on. */
fun guideLayerOf(time: TimeSignature, midis: List<Int>): ToneLayer {
    val measure = time.measure.ticks
    return ToneLayer(listOf(NoteTone(-3 * measure, measure, midis, MELODY_VOLUME)))
}

/**
 * Sticks counting two measures of [time] before the downbeat, then clicking every pulse through the first [ticks]
 * while [isClickOn].
 *
 * The count runs at half the pace of the pulse, then on it: the first measure on each half when its pulses split in
 * two, as "1… 2…" in 4/4, or on its downbeat alone, as in 3/4 or 6/8; the second on every pulse.
 */
fun drummerLayerOf(time: TimeSignature, ticks: Int, isClickOn: Boolean): ToneLayer {
    val measure = time.measure.ticks
    val pulse = time.pulse.ticks
    val pulses = measure / pulse
    val firstMeasure = if (pulses > 2 && pulses % 2 == 0) listOf(0, measure / 2) else listOf(0)
    val secondMeasure = List(pulses) { measure + it * pulse }
    val count = (firstMeasure + secondMeasure).map { it - 2 * measure }
    val click = if (isClickOn) (0 until ticks step pulse).toList() else emptyList()
    val drums = (count + click).map { tick ->
        DrumTone(tick, Drum.Sticks, if (tick % measure == 0) DrumHit.Accent.volume else DrumHit.Normal.volume)
    }
    return ToneLayer(drums)
}

/**
 * The tones of the tracks [trackIds] of this source, with both capos applied. A track or notation bound in [clips]
 * sounds no tones, as its clip plays in its place: a notation or arrangement silences each pass that starts within it,
 * and a whole track's clip silences the track in a source of the song.
 */
fun MusicSource.musicTonesOf(
    trackIds: Collection<TrackId>,
    clips: Collection<TrackClip> = emptyList(),
): List<ToneLayer> = tracks.filter { it.trackId in trackIds }.map { track ->
    val trackClips = clips.filter { it.trackId == track.trackId }
    if (targetId == null && trackClips.any { it.notationId == null }) return@map ToneLayer(emptyList())
    val clipped = track.spans.filter { span -> trackClips.any { it.notationId == span.notationId } }
    val silenced = track.passes.filter { pass -> clipped.any { pass.start in it.start until it.start + it.ticks } }
    val capo = (capo ?: 0) + (track.capo ?: 0)
    val heard = track.events.filter { event -> silenced.none { it.holds(event) } }
    ToneLayer(heard.mapNotNull { toneOf(it, key, capo) })
}

/** The first pitched tone of the track [trackId] of this source, with both capos applied, or `null` for none. */
fun MusicSource.firstToneOf(trackId: TrackId): NoteTone? =
    musicTonesOf(listOf(trackId)).firstOrNull()?.tones?.filterIsInstance<NoteTone>()?.firstOrNull()

/** The tone [event] sounds in [key] raised [capo] semitones, or `null` for a rest or a syllable. */
private fun toneOf(event: NotationEvent, key: Key, capo: Int): Tone? = when (event) {
    is NoteEvent -> event.midi?.let { NoteTone(event.tick, event.beats.ticks, listOf(it + capo), MELODY_VOLUME) }
    is ChordEvent -> event.chord?.let { chord ->
        NoteTone(event.tick, event.beats.ticks, chord.toMidi(key).map { it + capo }, CHORD_VOLUME)
    }
    is DrumEvent -> event.hit?.let { DrumTone(event.tick, event.drum, it.volume) }
    is VocalEvent -> null
}

/** Whether [event] is one of this pass, its pickup included. */
private fun NotationPass.holds(event: NotationEvent) = event.notationId == notation.notationId &&
    event.tick in start - notation.downbeat.ticks until start + notation.length.ticks

// a melody sounds over the chords beneath it
private const val MELODY_VOLUME = 1.0
private const val CHORD_VOLUME = 2.0 / 3
