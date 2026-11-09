package streetlight.web.model

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * The setup's acoustics as a calibration measured them through the mic named [micLabel], with levels in dB relative
 * to full scale.
 *
 * [rumbleDb] and [hissDb] are the noise below 120 Hz and from 4 to 16 kHz, and [humHz] the mains frequency when a
 * hum stands out. [reverbSeconds] is the room's RT60 and [clarityDb] its C50, each `null` when the measurement
 * lacked the range to find it. [arrivalDb] is how far the stimulus's arrival stands above the noise of the impulse
 * response, low when the mic never heard it. [processing] is what the browser applied despite the studio's constraints.
 */
data class AcousticProfile(
    val micLabel: String,
    val sampleRate: Int,
    val latencySeconds: Double,
    val arrivalDb: Double,
    val noiseFloorDb: Double,
    val rumbleDb: Double,
    val hissDb: Double,
    val humHz: Int?,
    val reverbSeconds: Double?,
    val clarityDb: Double?,
    val responseDb: Double,
    val peakDb: Double,
    val processing: List<BrowserProcessing>,
) {
    /** The range from the noise floor up to full scale. */
    val dynamicRangeDb get() = -noiseFloorDb
}

/**
 * Measures the [AcousticProfile] of [response], a capture during which [stimulus] began playing at [stimulusClock].
 * The capture's lead-in before the stimulus gives the noise.
 */
fun measureAcousticProfile(stimulus: PcmSample, stimulusClock: Double, response: CapturedPcm): AcousticProfile {
    val sampleRate = response.pcm.sampleRate
    val signal = response.pcm.channels.first()
    val playFrame = ((stimulusClock - response.startClock) * sampleRate).roundToInt().coerceIn(0, signal.size)
    val leadInStart = min((LEAD_IN_SKIP_SECONDS * sampleRate).toInt(), playFrame)
    val leadIn = signal.copyOfRange(leadInStart, playFrame)

    // find the impulse response, and where it arrives within the latency a setup can have
    val impulse = impulseResponseOf(signal, stimulus.channels.first())
    val lastFrame = min(impulse.size - 1, playFrame + (MAX_LATENCY_SECONDS * sampleRate).toInt())
    val arrivalFrame = (playFrame..lastFrame).maxByOrNull { abs(impulse[it]) } ?: playFrame

    // take the decay from just before arrival, through the lags the whole stimulus was captured for
    val decayStart = max(arrivalFrame - (ONSET_SECONDS * sampleRate).toInt(), 0)
    val decayEnd = max(signal.size - stimulus.frameCount, decayStart)
    val decay = impulse.copyOfRange(decayStart, decayEnd)
    val decayLength = decay.findNoiseEnd(sampleRate)

    val noise = NoiseSpectrum.of(leadIn, sampleRate)

    return AcousticProfile(
        micLabel = response.micLabel,
        sampleRate = sampleRate,
        latencySeconds = (arrivalFrame - playFrame).toDouble() / sampleRate,
        arrivalDb = impulse.readArrivalDb(arrivalFrame, leadInStart, playFrame),
        noiseFloorDb = leadIn.readRmsDb(0, leadIn.size),
        rumbleDb = noise.readBandDb(20.0, 120.0),
        hissDb = noise.readBandDb(4_000.0, 16_000.0),
        humHz = noise.findHumHz(),
        reverbSeconds = decay.measureReverbSeconds(decayLength, sampleRate),
        clarityDb = decay.measureClarityDb(decayLength, sampleRate),
        responseDb = signal.readRmsDb(arrivalFrame, arrivalFrame + stimulus.frameCount),
        peakDb = toDb(signal.maxOfOrNull { abs(it) }?.toDouble() ?: 0.0),
        processing = response.processing,
    )
}

/** The power of noise in each frequency bin [binHz] wide, averaged over overlapping windows. */
private class NoiseSpectrum(val power: DoubleArray, val binHz: Double) {
    /** The level of the noise from [lowHz] up to [highHz]. */
    fun readBandDb(lowHz: Double, highHz: Double): Double {
        var sum = 0.0
        for (bin in power.indices) {
            if (bin * binHz >= lowHz && bin * binHz < highHz) sum += power[bin]
        }
        return toPowerDb(sum)
    }

    /** The mains frequency whose hum stands out from the noise around it, or `null` when neither does. */
    fun findHumHz() = MAINS_HZ
        .map { mainsHz -> mainsHz to (1..HUM_HARMONICS).maxOf { readProminence(mainsHz * it.toDouble()) } }
        .filter { (_, prominence) -> prominence >= HUM_PROMINENCE }
        .maxByOrNull { (_, prominence) -> prominence }
        ?.first

    // the power at a frequency over the median of its neighbors, leaving out the bins its peak spreads into
    private fun readProminence(hz: Double): Double {
        val center = (hz / binHz).roundToInt()
        val peak = (center - 1..center + 1).maxOf { power.getOrElse(it) { 0.0 } }
        val reach = (HUM_NEIGHBOR_HZ / binHz).roundToInt()
        val neighbors = (center - reach..center + reach)
            .filter { abs(it - center) > HUM_PEAK_BINS && it in power.indices }
            .map { power[it] }
            .sorted()
        val median = neighbors.getOrNull(neighbors.size / 2) ?: return 0.0
        return if (median > 0) peak / median else 0.0
    }

    companion object {
        /** The spectrum of [samples], recorded at [sampleRate]. */
        fun of(samples: FloatArray, sampleRate: Int): NoiseSpectrum {
            val size = SPECTRUM_SIZE
            val window = DoubleArray(size) { 0.5 - 0.5 * cos(2 * PI * it / size) }
            val windowPower = window.sumOf { it * it }
            val power = DoubleArray(size / 2 + 1)

            // sum the power of each window, stepping half a window at a time
            var windowCount = 0
            var start = 0
            while (start + size <= samples.size) {
                val real = DoubleArray(size) { samples[start + it] * window[it] }
                val imaginary = DoubleArray(size)
                transformFourier(real, imaginary)
                for (bin in power.indices) power[bin] += real[bin] * real[bin] + imaginary[bin] * imaginary[bin]
                windowCount++
                start += size / 2
            }

            // scale to mean-square power, counting the mirrored half of each bin but the ends
            if (windowCount > 0) {
                for (bin in power.indices) {
                    val sides = if (bin == 0 || bin == size / 2) 1 else 2
                    power[bin] = power[bin] * sides / (windowCount * size * windowPower)
                }
            }
            return NoiseSpectrum(power, sampleRate.toDouble() / size)
        }
    }
}

/**
 * The peak of this impulse response at [arrivalFrame] over its level at the lags from [noiseStart] until [noiseEnd],
 * before the stimulus played, where nothing but noise can answer it.
 */
private fun DoubleArray.readArrivalDb(arrivalFrame: Int, noiseStart: Int, noiseEnd: Int): Double {
    val noisePower = readMeanSquare(noiseStart, noiseEnd)
    if (noisePower == 0.0) return 0.0
    return toPowerDb(this[arrivalFrame] * this[arrivalFrame] / noisePower)
}

/** The length of this decay before it sinks into the noise of its own tail. */
private fun DoubleArray.findNoiseEnd(sampleRate: Int): Int {
    val tailStart = (size * (1 - NOISE_TAIL_SHARE)).toInt()
    val noisePower = readMeanSquare(tailStart, size)
    val step = (DECAY_STEP_SECONDS * sampleRate).toInt()
    var start = step
    while (start + step <= tailStart) {
        if (readMeanSquare(start, start + step) <= noisePower * NOISE_MARGIN) return start
        start += step
    }
    return tailStart
}

/** The RT60 of this decay through [length], from its fall between -5 and -25 dB, or `null` without that range. */
private fun DoubleArray.measureReverbSeconds(length: Int, sampleRate: Int): Double? {
    // integrate the energy backward from the end, as a level below the total
    val remaining = DoubleArray(length)
    var energy = 0.0
    for (index in length - 1 downTo 0) {
        energy += this[index] * this[index]
        remaining[index] = energy
    }
    if (energy == 0.0) return null
    val levels = DoubleArray(length) { toPowerDb(remaining[it] / energy) }

    // fit a line to the fall, and extend it to 60 dB
    val start = levels.indexOfFirst { it <= FIT_START_DB }
    val end = levels.indexOfFirst { it <= FIT_END_DB }
    if (start < 0 || end <= start) return null
    val slope = readSlope(levels, start, end) * sampleRate
    return if (slope < 0) -60 / slope else null
}

/** The C50 of this decay through [length]: its energy in the first 50 ms over the energy after, or `null` without any. */
private fun DoubleArray.measureClarityDb(length: Int, sampleRate: Int): Double? {
    val split = min((CLARITY_SECONDS * sampleRate).toInt(), length)
    val early = readMeanSquare(0, split) * split
    val late = readMeanSquare(split, length) * (length - split)
    return if (late > 0) toPowerDb(early / late) else null
}

// the slope of the least-squares line through the values from start until end, per index
private fun readSlope(values: DoubleArray, start: Int, end: Int): Double {
    val count = end - start
    val meanIndex = (start + end - 1) / 2.0
    val meanValue = (start until end).sumOf { values[it] } / count
    var covariance = 0.0
    var variance = 0.0
    for (index in start until end) {
        covariance += (index - meanIndex) * (values[index] - meanValue)
        variance += (index - meanIndex) * (index - meanIndex)
    }
    return covariance / variance
}

private fun DoubleArray.readMeanSquare(start: Int, end: Int): Double {
    if (end <= start) return 0.0
    var sum = 0.0
    for (index in start until end) sum += this[index] * this[index]
    return sum / (end - start)
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
private fun toPowerDb(power: Double) = 10 * log10(max(power, MIN_LEVEL * MIN_LEVEL))

private const val MAX_LATENCY_SECONDS = 1.0
private const val MIN_LEVEL = 1e-6

// the start of a capture can hold the mic settling
private const val LEAD_IN_SKIP_SECONDS = 0.1

// ~2.9 Hz bins at 48 kHz, fine enough to tell 50 Hz hum from 60 Hz
private const val SPECTRUM_SIZE = 16_384
private val MAINS_HZ = listOf(50, 60)
private const val HUM_HARMONICS = 3
private const val HUM_NEIGHBOR_HZ = 30.0
private const val HUM_PEAK_BINS = 2
// 10 dB above the neighbors
private const val HUM_PROMINENCE = 10.0

// the impulse response's rise begins just before its peak
private const val ONSET_SECONDS = 0.001
private const val NOISE_TAIL_SHARE = 0.1
private const val DECAY_STEP_SECONDS = 0.01
// within 3 dB of the tail's noise
private const val NOISE_MARGIN = 2.0
private const val FIT_START_DB = -5.0
private const val FIT_END_DB = -25.0
private const val CLARITY_SECONDS = 0.05
