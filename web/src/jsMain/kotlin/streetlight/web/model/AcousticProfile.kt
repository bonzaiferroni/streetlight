package streetlight.web.model

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** The setup's acoustics as a calibration measured them, its levels in dB relative to full scale. */
data class AcousticProfile(
    val sampleRate: Int,
    val latencySeconds: Double,
    val noiseFloorDb: Double,
    val responseDb: Double,
    val peakDb: Double,
) {
    /** How far the response stood above the noise floor. */
    val signalToNoiseDb get() = responseDb - noiseFloorDb
}

/**
 * Measures the [AcousticProfile] of [response], a capture during which [stimulus] began playing at [stimulusClock].
 * The capture's lead-in before the stimulus gives the noise floor.
 */
fun measureAcousticProfile(stimulus: PcmSample, stimulusClock: Double, response: CapturedPcm): AcousticProfile {
    val sampleRate = response.pcm.sampleRate
    val signal = response.pcm.channels.first()
    val playFrame = ((stimulusClock - response.startClock) * sampleRate).roundToInt().coerceIn(0, signal.size)

    // find where the stimulus arrives, no earlier than it played and within the latency a setup can have
    val correlation = crossCorrelationOf(signal, stimulus.channels.first())
    val lastFrame = min(correlation.size - 1, playFrame + (MAX_LATENCY_SECONDS * sampleRate).toInt())
    val arrivalFrame = (playFrame..lastFrame).maxByOrNull { abs(correlation[it]) } ?: playFrame

    return AcousticProfile(
        sampleRate = sampleRate,
        latencySeconds = (arrivalFrame - playFrame).toDouble() / sampleRate,
        noiseFloorDb = signal.readRmsDb(0, playFrame),
        responseDb = signal.readRmsDb(arrivalFrame, arrivalFrame + stimulus.frameCount),
        peakDb = toDb(signal.maxOfOrNull { abs(it) }?.toDouble() ?: 0.0),
    )
}

/** The root mean square level of the samples from [start] until [end], in dB relative to full scale. */
private fun FloatArray.readRmsDb(start: Int, end: Int): Double {
    val last = min(end, size)
    if (last <= start) return toDb(0.0)
    var sum = 0.0
    for (index in start until last) sum += this[index].toDouble() * this[index]
    return toDb(sqrt(sum / (last - start)))
}

private fun toDb(level: Double) = 20 * log10(max(level, MIN_LEVEL))

private const val MAX_LATENCY_SECONDS = 1.0
private const val MIN_LEVEL = 1e-6
