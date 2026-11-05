@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import kampfire.model.Tap
import kampfire.model.storeOf
import streetlight.model.data.Beats
import streetlight.model.data.Chord
import streetlight.model.data.ChordQuality
import streetlight.model.data.Groove
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
import web.audio.AudioContext
import web.audio.OscillatorNode
import web.audio.GainNode
import web.events.EventHandler
import kotlin.math.pow
import kotlin.math.sqrt

/** Plays the tracks of a song through the speakers, each note a few harmonics shaped by an envelope. */
class TrackPlayer(
    private val audio: StudioAudio,
) {
    private val oscillators = mutableListOf<OscillatorNode>()
    private var bus: GainNode? = null

    private val playingStore = storeOf(false)
    val isPlayingState: Tap<Boolean> = playingStore

    /**
     * Plays [playing], tracks of a song of [tracks], through the arrangement of [music]. Each section lasts as long as
     * the longest track that defines it, and a track rests through a section it lacks or runs short of. Any playback
     * already running is stopped first.
     */
    fun play(music: Music, tracks: List<Track>, playing: List<Track>) {
        stop()
        val tones = mutableListOf<Tone>()
        var onset = 0
        for (sectionId in tracks.sectionOrderOf(music)) {
            playing.forEach { track ->
                var tick = onset
                track.sectionsOf(sectionId)?.forEach { section ->
                    tones.addAll(tonesOf(section, tick))
                    tick += section.length.ticks
                }
            }
            onset += tracks.lengthOf(sectionId)?.ticks ?: 0
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
            val voiceLevel = tone.level / sqrt(tone.midi.size.toDouble())
            tone.midi.forEach { midi -> voice(context, runBus, midi, start, duration, voiceLevel) }
        }
        playingStore.set(oscillators.isNotEmpty())
    }

    /** The tones of [section] placed at [onset], its pickup sounding before it. */
    private fun tonesOf(section: Section, onset: Int): List<Tone> = when (section) {
        is Phrase -> section.pickup.toTones(onset - section.pickup.lengthOf { it.beats }, MELODY_LEVEL, { it.beats }) {
            it.pitch?.let { pitch -> listOf(pitch.midi) }
        } + section.notes.toTones(onset, MELODY_LEVEL, { it.beats }) { it.pitch?.let { pitch -> listOf(pitch.midi) } }
        is Progression -> section.pickup.toTones(onset - section.pickup.lengthOf { it.beats }, ACCOMPANIMENT_LEVEL, { it.beats }) {
            it.chord?.toMidi()
        } + section.notes.toTones(onset, ACCOMPANIMENT_LEVEL, { it.beats }) { it.chord?.toMidi() }
        is Stanza, is Groove -> emptyList()
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

/** The MIDI numbers of [midi] sounding from [startTick] for [ticks], peaking together at [level]. */
private class Tone(val startTick: Int, val ticks: Int, val level: Double, val midi: List<Int>)

/** The notes laid end to end from [onset], each a tone of the MIDI numbers [midiOf] gives, or a rest for `null`. */
private fun <N> List<N>.toTones(onset: Int, level: Double, beatsOf: (N) -> Beats, midiOf: (N) -> List<Int>?): List<Tone> {
    var tick = onset
    return mapNotNull { note ->
        val ticks = beatsOf(note).ticks
        val tone = midiOf(note)?.let { Tone(tick, ticks, level, it) }
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
