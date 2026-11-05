@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.data.Beats
import streetlight.model.data.Chord
import streetlight.model.data.ChordQuality
import streetlight.model.data.ChordTrack
import streetlight.model.data.MelodyTrack
import streetlight.model.data.Music
import streetlight.model.data.Pattern
import streetlight.model.data.Seventh
import streetlight.model.data.PatternId
import web.audio.AudioContext
import web.audio.OscillatorNode
import web.audio.GainNode
import web.events.EventHandler
import kotlin.math.pow
import kotlin.math.sqrt

/** Plays a track's arrangement through the speakers, each note a few harmonics shaped by an envelope. */
class TrackPlayer(
    private val audio: StudioAudio,
) {
    private val oscillators = mutableListOf<OscillatorNode>()
    private var bus: GainNode? = null

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /** Plays the melody of [track] at the tempo of [music]. Any playback already running is stopped first. */
    fun play(track: MelodyTrack, music: Music) = playPatterns(track.patterns, track.arrangement, music, MELODY_LEVEL) { phrase, onset ->
        phrase.pickup.toTones(onset - phrase.pickup.lengthOf { it.beats }, { it.beats }) { it.pitch?.let { pitch -> listOf(pitch.midi) } } +
            phrase.notes.toTones(onset, { it.beats }) { it.pitch?.let { pitch -> listOf(pitch.midi) } }
    }

    /** Plays the chords of [track] at the tempo of [music], each as a block of its tones. */
    fun play(track: ChordTrack, music: Music) = playPatterns(track.patterns, track.arrangement, music, ACCOMPANIMENT_LEVEL) { progression, onset ->
        progression.pickup.toTones(onset - progression.pickup.lengthOf { it.beats }, { it.beats }) { it.chord?.toMidi() } +
            progression.notes.toTones(onset, { it.beats }) { it.chord?.toMidi() }
    }

    /** Stops every tone playing, silencing their release tails at once. */
    fun stop() {
        bus?.disconnect()
        bus = null
        val current = oscillators.toList()
        oscillators.clear()
        current.forEach { oscillator ->
            oscillator.onended = null
            oscillator.stop()
        }
        playingStore.set(false)
    }

    /**
     * Plays [patterns] in [arrangement], or each once in order when it is empty, with [tonesOf] giving the tones of a
     * pattern placed at its onset in ticks. A tone peaks at [level], shared among the pitches sounding together.
     */
    private fun <P : Pattern> playPatterns(
        patterns: List<P>,
        arrangement: List<PatternId>,
        music: Music,
        level: Double,
        tonesOf: (P, Int) -> List<Tone>,
    ) {
        stop()
        val order = arrangement.mapNotNull { id -> patterns.firstOrNull { it.patternId == id } }.ifEmpty { patterns }
        var onset = 0
        val tones = order.flatMap { pattern ->
            tonesOf(pattern, onset).also { onset += pattern.length.ticks }
        }
        if (tones.isEmpty()) return

        val context = audio.readContext()
        val runBus = context.createGain().also { it.connect(context.destination) }
        bus = runBus
        val secondsPerTick = 60.0 / music.bpm / music.time.beat.ticks
        // a pickup sounds before the first downbeat, so the earliest tone sets the start
        val firstTick = tones.minOf { it.startTick }
        val startClock = context.currentTime + START_DELAY_SECONDS - firstTick * secondsPerTick
        tones.forEach { tone ->
            val start = startClock + tone.startTick * secondsPerTick
            val duration = tone.ticks * secondsPerTick
            val voiceLevel = level / sqrt(tone.midi.size.toDouble())
            tone.midi.forEach { midi -> voice(context, runBus, midi, start, duration, voiceLevel) }
        }
        playingStore.set(oscillators.isNotEmpty())
    }

    /**
     * Sounds [midi] into [bus] from [start] for [duration] seconds at [level]: its first harmonics under one envelope
     * that blooms, relaxes into a softer sustain, and releases just before the note ends.
     */
    private fun voice(context: AudioContext, bus: GainNode, midi: Int, start: Double, duration: Double, level: Double) {
        val envelope = context.createGain()
        envelope.gain.setValueAtTime(0f, start)
        envelope.gain.linearRampToValueAtTime(level.toFloat(), start + ATTACK_SECONDS)
        envelope.gain.setTargetAtTime((level * SUSTAIN_RATIO).toFloat(), start + SETTLE_SECONDS, SETTLE_TIME_CONSTANT)
        envelope.gain.setTargetAtTime(0f, start + duration * RELEASE_POINT, RELEASE_TIME_CONSTANT)
        envelope.connect(bus)

        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        HARMONICS.forEach { (multiple, amplitude) ->
            val oscillator = context.createOscillator()
            oscillator.frequency.value = (frequency * multiple).toFloat()
            val gain = context.createGain()
            gain.gain.value = amplitude.toFloat()
            oscillator.connect(gain)
            gain.connect(envelope)
            oscillator.onended = EventHandler { endOscillator(oscillator) }
            oscillator.start(start)
            // the release tail outlasts the note, so the oscillators run on until it has faded
            oscillator.stop(start + duration + TAIL_SECONDS)
            oscillators.add(oscillator)
        }
    }

    private fun endOscillator(oscillator: OscillatorNode) {
        oscillators.remove(oscillator)
        if (oscillators.isEmpty()) playingStore.set(false)
    }
}

/** The MIDI numbers of [midi] sounding from [startTick] for [ticks]. */
private class Tone(val startTick: Int, val ticks: Int, val midi: List<Int>)

/** The notes laid end to end from [onset], each a tone of the MIDI numbers [midiOf] gives, or a rest for `null`. */
private fun <N> List<N>.toTones(onset: Int, beatsOf: (N) -> Beats, midiOf: (N) -> List<Int>?): List<Tone> {
    var tick = onset
    return mapNotNull { note ->
        val ticks = beatsOf(note).ticks
        val tone = midiOf(note)?.let { Tone(tick, ticks, it) }
        tick += ticks
        tone
    }
}

private fun <N> List<N>.lengthOf(beatsOf: (N) -> Beats) = sumOf { beatsOf(it).ticks }

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
private const val START_DELAY_SECONDS = 0.1
private const val MELODY_LEVEL = 0.18
private const val ACCOMPANIMENT_LEVEL = 0.12

/** The multiple of the fundamental and the relative level of each harmonic a voice sounds. */
private val HARMONICS = listOf(1 to 1.0, 2 to 0.3, 3 to 0.12)

private const val ATTACK_SECONDS = 0.03
private const val SETTLE_SECONDS = 0.05
private const val SETTLE_TIME_CONSTANT = 0.2f
private const val SUSTAIN_RATIO = 0.65

/** The point in a note, as a share of its length, where its release begins. */
private const val RELEASE_POINT = 0.92
private const val RELEASE_TIME_CONSTANT = 0.05f
private const val TAIL_SECONDS = 0.3
