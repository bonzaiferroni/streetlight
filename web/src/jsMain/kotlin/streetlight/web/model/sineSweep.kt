package streetlight.web.model

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin

/**
 * An exponential sine sweep at [sampleRate], rising from [startHz] to [endHz] over [seconds] at [level], with each
 * octave taking equal time. Its ends fade over [fadeSeconds] so it starts and stops without a click.
 */
fun sineSweepOf(
    sampleRate: Int,
    seconds: Double = 3.0,
    startHz: Double = 20.0,
    endHz: Double = 20_000.0,
    level: Float = 0.25f,
    fadeSeconds: Double = 0.05,
): PcmSample {
    val frameCount = (seconds * sampleRate).toInt()
    val fadeFrames = (fadeSeconds * sampleRate).toInt()
    val octaveRate = ln(endHz / startHz)
    val phaseScale = 2 * PI * startHz * seconds / octaveRate

    val samples = FloatArray(frameCount) { frame ->
        val time = frame.toDouble() / sampleRate
        val phase = phaseScale * (exp(time / seconds * octaveRate) - 1)
        val fade = min(1.0, min(frame, frameCount - 1 - frame).toDouble() / fadeFrames)
        (sin(phase) * fade).toFloat() * level
    }
    return PcmSample(sampleRate, listOf(samples))
}
