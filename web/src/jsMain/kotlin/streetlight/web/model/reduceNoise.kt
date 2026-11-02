package streetlight.web.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max

/**
 * This sample with its steady noise lowered by spectral gating. The noise of each frequency is learned from the
 * sample's quietest moments, and each moment keeps that frequency in proportion to how far it rises above the noise.
 */
fun PcmSample.reduceNoise() = PcmSample(sampleRate, channels.map { it.reduceNoise() })

private fun FloatArray.reduceNoise(): FloatArray {
    val binCount = FRAME_SIZE / 2 + 1
    val window = DoubleArray(FRAME_SIZE) { 0.5 - 0.5 * cos(2 * PI * it / FRAME_SIZE) }
    val padded = FloatArray(size + 2 * FRAME_SIZE).also { copyInto(it, FRAME_SIZE) }
    val frameCount = (padded.size - FRAME_SIZE) / HOP_SIZE + 1

    fun transformFrame(frame: Int): Pair<DoubleArray, DoubleArray> {
        val start = frame * HOP_SIZE
        val real = DoubleArray(FRAME_SIZE) { padded[start + it] * window[it] }
        val imaginary = DoubleArray(FRAME_SIZE)
        transformFourier(real, imaginary)
        return real to imaginary
    }

    // learn the noise of each bin from the frames inside the sample
    val powers = FloatArray(frameCount * binCount)
    for (frame in 0 until frameCount) {
        val (real, imaginary) = transformFrame(frame)
        for (bin in 0 until binCount) {
            powers[frame * binCount + bin] = (real[bin] * real[bin] + imaginary[bin] * imaginary[bin]).toFloat()
        }
    }
    val innerFrames = (0 until frameCount).filter { it * HOP_SIZE >= FRAME_SIZE && it * HOP_SIZE + FRAME_SIZE <= FRAME_SIZE + size }
    if (innerFrames.isEmpty()) return copyOf()
    val noise = DoubleArray(binCount) { bin ->
        val sorted = innerFrames.map { powers[it * binCount + bin] }.sorted()
        sorted[(sorted.size * NOISE_PERCENTILE).toInt()].toDouble()
    }

    // scale each bin by its gain, holding gains briefly as they fall, and overlap the frames back together
    val output = DoubleArray(padded.size)
    val heldGain = DoubleArray(binCount) { 1.0 }
    for (frame in 0 until frameCount) {
        val (real, imaginary) = transformFrame(frame)
        for (bin in 0 until binCount) {
            val power = powers[frame * binCount + bin].toDouble()
            val openGain = if (power > 0) 1 - OVERSUBTRACTION * noise[bin] / power else 0.0
            val gain = max(max(openGain, GAIN_FLOOR), heldGain[bin] * GAIN_RELEASE)
            heldGain[bin] = gain
            real[bin] *= gain
            imaginary[bin] *= gain
            if (bin in 1 until FRAME_SIZE / 2) {
                real[FRAME_SIZE - bin] *= gain
                imaginary[FRAME_SIZE - bin] *= gain
            }
        }
        transformFourier(real, imaginary, inverse = true)
        val start = frame * HOP_SIZE
        for (index in 0 until FRAME_SIZE) output[start + index] += real[index] / FRAME_SIZE * window[index]
    }

    return FloatArray(size) { (output[FRAME_SIZE + it] / WINDOW_OVERLAP_SUM).toFloat() }
}

private const val FRAME_SIZE = 2048
private const val HOP_SIZE = 512
// the sum of the squared window across its overlaps, at a quarter-frame hop
private const val WINDOW_OVERLAP_SUM = 1.5
// the noise is the level a frequency stays above for nine-tenths of the sample
private const val NOISE_PERCENTILE = 0.1
private const val OVERSUBTRACTION = 1.5
// noise is lowered by up to 20 dB, not removed, which keeps the background natural
private const val GAIN_FLOOR = 0.1
private const val GAIN_RELEASE = 0.6
