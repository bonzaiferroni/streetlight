@file:OptIn(ExperimentalWasmJsInterop::class)

package streetlight.web.model

import js.buffer.ArrayBuffer
import js.typedarrays.Float32Array
import streetlight.model.data.Drum
import streetlight.model.data.DrumHit
import web.audio.AudioBuffer
import web.audio.AudioContext
import web.audio.AudioNode
import web.audio.AudioScheduledSourceNode
import web.audio.BiquadFilterType
import web.audio.OscillatorType
import web.audio.bandpass
import web.audio.highpass
import web.audio.triangle
import kotlin.random.Random

/** The synthesized drums: a falling tone for the kick and toms, and filtered noise for the snare and cymbals. */
class DrumKit {
    private var noise: AudioBuffer? = null

    /** Strikes [drum] with [hit] into [bus] at [start], and returns the sources it started so they can be stopped. */
    fun strike(
        context: AudioContext,
        bus: AudioNode,
        drum: Drum,
        hit: DrumHit,
        start: Double,
    ): List<AudioScheduledSourceNode> {
        val scale = when (hit) {
            DrumHit.Normal -> 1.0
            DrumHit.Accent -> ACCENT_SCALE
            DrumHit.Ghost -> GHOST_SCALE
        }
        return when (drum) {
            Drum.Kick -> listOf(fallingTone(context, bus, start, 150f, 50f, 0.1, 0.4, 0.5 * scale))
            Drum.Tom -> listOf(fallingTone(context, bus, start, 120f, 80f, 0.15, 0.3, 0.35 * scale))
            Drum.Snare -> listOf(
                noise(context, bus, start, BiquadFilterType.highpass, 1000f, 0.18, 0.25 * scale),
                bodyTone(context, bus, start, 180f, 0.1, 0.15 * scale),
            )
            Drum.HiHat -> listOf(noise(context, bus, start, BiquadFilterType.highpass, 7000f, 0.05, 0.12 * scale))
            Drum.Crash -> listOf(noise(context, bus, start, BiquadFilterType.highpass, 5000f, 1.5, 0.15 * scale))
            Drum.Ride -> listOf(noise(context, bus, start, BiquadFilterType.bandpass, 8000f, 0.8, 0.1 * scale))
        }
    }

    /** A sine falling from [from] to [to] hertz over [drop] seconds, fading out over [decay] seconds from [level]. */
    private fun fallingTone(
        context: AudioContext,
        bus: AudioNode,
        start: Double,
        from: Float,
        to: Float,
        drop: Double,
        decay: Double,
        level: Double,
    ): AudioScheduledSourceNode {
        val oscillator = context.createOscillator()
        oscillator.frequency.setValueAtTime(from, start)
        oscillator.frequency.exponentialRampToValueAtTime(to, start + drop)
        oscillator.connect(decayGain(context, bus, start, decay, level))
        oscillator.start(start)
        oscillator.stop(start + decay + TAIL_SECONDS)
        return oscillator
    }

    /** A triangle at [frequency] hertz fading out over [decay] seconds from [level], the body under a snare's noise. */
    private fun bodyTone(
        context: AudioContext,
        bus: AudioNode,
        start: Double,
        frequency: Float,
        decay: Double,
        level: Double,
    ): AudioScheduledSourceNode {
        val oscillator = context.createOscillator()
        oscillator.type = OscillatorType.triangle
        oscillator.frequency.value = frequency
        oscillator.connect(decayGain(context, bus, start, decay, level))
        oscillator.start(start)
        oscillator.stop(start + decay + TAIL_SECONDS)
        return oscillator
    }

    /** White noise through a filter of [type] at [frequency] hertz, fading out over [decay] seconds from [level]. */
    private fun noise(
        context: AudioContext,
        bus: AudioNode,
        start: Double,
        type: BiquadFilterType,
        frequency: Float,
        decay: Double,
        level: Double,
    ): AudioScheduledSourceNode {
        val source = context.createBufferSource()
        source.buffer = readNoise(context)
        val filter = context.createBiquadFilter()
        filter.type = type
        filter.frequency.value = frequency
        source.connect(filter)
        filter.connect(decayGain(context, bus, start, decay, level))
        source.start(start)
        source.stop(start + decay + TAIL_SECONDS)
        return source
    }

    /** A gain into [bus] that starts at [level] and fades to silence over [decay] seconds. */
    private fun decayGain(
        context: AudioContext,
        bus: AudioNode,
        start: Double,
        decay: Double,
        level: Double,
    ): AudioNode {
        val gain = context.createGain()
        gain.gain.setValueAtTime(level.toFloat(), start)
        gain.gain.exponentialRampToValueAtTime(SILENCE, start + decay)
        gain.connect(bus)
        return gain
    }

    /** A second of white noise, made once for [context]. */
    private fun readNoise(context: AudioContext): AudioBuffer = noise ?: run {
        val sampleRate = context.sampleRate
        val samples = FloatArray(sampleRate.toInt()) { Random.nextFloat() * 2 - 1 }
        val buffer = context.createBuffer(1, samples.size, sampleRate)
        buffer.copyToChannel(samples.unsafeCast<Float32Array<ArrayBuffer>>(), 0)
        buffer.also { noise = it }
    }
}

private const val ACCENT_SCALE = 1.3
private const val GHOST_SCALE = 0.4

/** The level an exponential fade ends on, since it cannot reach zero. */
private const val SILENCE = 0.001f
private const val TAIL_SECONDS = 0.05
