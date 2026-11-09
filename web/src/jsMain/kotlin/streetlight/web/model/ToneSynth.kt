package streetlight.web.model

import streetlight.model.music.Drum
import streetlight.model.music.DrumHit
import web.audio.AudioNode
import web.audio.AudioScheduledSourceNode
import web.audio.BaseAudioContext
import kotlin.math.pow

/** The synthesized tones and drums of the studio, sounded into any context, live or offline. */
class ToneSynth {
    private val kit = DrumKit()

    /**
     * Sounds [midi] into [bus] from [start] for [duration] seconds at [level]: its first harmonics under one envelope
     * that blooms, relaxes into a softer sustain, and releases just before the note ends. Returns the oscillators.
     */
    fun voice(
        context: BaseAudioContext,
        bus: AudioNode,
        midi: Int,
        start: Double,
        duration: Double,
        level: Double,
    ): List<AudioScheduledSourceNode> {
        val envelope = context.createGain()
        envelope.gain.setValueAtTime(0f, start)
        envelope.gain.linearRampToValueAtTime(level.toFloat(), start + ATTACK_SECONDS)
        envelope.gain.setTargetAtTime((level * SUSTAIN_RATIO).toFloat(), start + SETTLE_SECONDS, SETTLE_TIME_CONSTANT)
        envelope.gain.setTargetAtTime(0f, start + duration * RELEASE_POINT, RELEASE_TIME_CONSTANT)
        envelope.connect(bus)

        val frequency = 440.0 * 2.0.pow((midi - 69) / 12.0)
        return HARMONICS.map { (multiple, amplitude) ->
            val oscillator = context.createOscillator()
            oscillator.frequency.value = (frequency * multiple).toFloat()
            val gain = context.createGain()
            gain.gain.value = amplitude.toFloat()
            oscillator.connect(gain)
            gain.connect(envelope)
            oscillator.start(start)
            // the release tail outlasts the note, so the oscillators run on until it has faded
            oscillator.stop(start + duration + TAIL_SECONDS)
            oscillator
        }
    }
    /** Strikes [drum] with [hit] into [bus] at [start]. Returns the sources. */
    fun strike(context: BaseAudioContext, bus: AudioNode, drum: Drum, hit: DrumHit, start: Double) =
        kit.strike(context, bus, drum, hit, start)

    /** Clicks sticks into [bus] at [start], louder for an [accent]. Returns the sources. */
    fun sticks(context: BaseAudioContext, bus: AudioNode, start: Double, accent: Boolean) =
        kit.sticks(context, bus, start, accent)

    companion object {
        const val MELODY_LEVEL = 0.18
        const val ACCOMPANIMENT_LEVEL = 0.12
    }
}

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
