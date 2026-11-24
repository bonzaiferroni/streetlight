package streetlight.web.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/**
 * A pitch reading of a frame of a capture, centered [seconds] into it: its pitch in [hertz], or `null` when unvoiced,
 * how periodic it is as [confidence] from 0 to 1, and its loudness as [rms].
 */
data class PitchFrame(
    val seconds: Double,
    val hertz: Double?,
    val confidence: Double,
    val rms: Double,
)

/**
 * Reads the pitch and the attacks of a single voice from the blocks of a capture at [sampleRate] as they arrive: a
 * [PitchFrame] for each hop into [frames] by YIN, and the seconds of each attack into [onsets] by spectral flux.
 */
class MusicAnalyzer(
    private val sampleRate: Int,
) : BlockSink {
    val frames: List<PitchFrame>
        field = mutableListOf<PitchFrame>()
    val onsets: List<Double>
        field = mutableListOf<Double>()

    /** The seconds between the starts of two frames. */
    val hopSeconds get() = HOP_SIZE.toDouble() / sampleRate

    private val buffer = FloatArray(FRAME_SIZE)
    private var filled = 0
    private var frameIndex = 0

    private val minLag = sampleRate / MAX_HERTZ
    private val maxLag = minOf(sampleRate / MIN_HERTZ, FRAME_SIZE - WINDOW_SIZE)

    private var lastSpectrum: DoubleArray? = null
    private val fluxes = mutableListOf<Double>()

    override fun add(block: Array<FloatArray>) {
        val samples = block.firstOrNull() ?: return
        for (sample in samples) {
            buffer[filled++] = sample
            if (filled == FRAME_SIZE) {
                readFrame()
                buffer.copyInto(buffer, 0, HOP_SIZE, FRAME_SIZE)
                filled = FRAME_SIZE - HOP_SIZE
            }
        }
    }

    private fun readFrame() {
        val seconds = (frameIndex * HOP_SIZE + FRAME_SIZE / 2).toDouble() / sampleRate
        val real = DoubleArray(FFT_SIZE) { if (it < FRAME_SIZE) buffer[it].toDouble() else 0.0 }
        val imaginary = DoubleArray(FFT_SIZE)
        transformFourier(real, imaginary)
        frames.add(pitchFrameOf(seconds, real, imaginary))
        readOnset(seconds, real, imaginary)
        frameIndex++
    }

    /** The YIN reading of the frame, from its spectrum [real] and [imaginary]. */
    private fun pitchFrameOf(seconds: Double, real: DoubleArray, imaginary: DoubleArray): PitchFrame {
        // the window's correlation with the frame at each lag, as the window's spectrum against the frame's
        val windowReal = DoubleArray(FFT_SIZE) { if (it < WINDOW_SIZE) buffer[it].toDouble() else 0.0 }
        val windowImaginary = DoubleArray(FFT_SIZE)
        transformFourier(windowReal, windowImaginary)
        for (index in 0 until FFT_SIZE) {
            val r = windowReal[index] * real[index] + windowImaginary[index] * imaginary[index]
            val i = windowReal[index] * imaginary[index] - windowImaginary[index] * real[index]
            windowReal[index] = r
            windowImaginary[index] = i
        }
        transformFourier(windowReal, windowImaginary, inverse = true)

        val squares = DoubleArray(FRAME_SIZE + 1)
        for (index in 0 until FRAME_SIZE) squares[index + 1] = squares[index] + buffer[index] * buffer[index]
        val windowEnergy = squares[WINDOW_SIZE]
        val rms = sqrt(squares[FRAME_SIZE] / FRAME_SIZE)

        // the cumulative mean normalized difference, which dips toward 0 at the period
        val normalized = DoubleArray(maxLag + 1)
        normalized[0] = 1.0
        var runningSum = 0.0
        for (lag in 1..maxLag) {
            val correlation = windowReal[lag] / FFT_SIZE
            val difference = windowEnergy + (squares[lag + WINDOW_SIZE] - squares[lag]) - 2 * correlation
            runningSum += difference
            normalized[lag] = if (runningSum > 0) difference * lag / runningSum else 1.0
        }

        var lag = (minLag..maxLag).firstOrNull { normalized[it] < YIN_THRESHOLD }
            ?: return PitchFrame(seconds, null, (1 - (minLag..maxLag).minOf { normalized[it] }).coerceIn(0.0, 1.0), rms)
        // follow the dip down to its bottom
        while (lag < maxLag && normalized[lag + 1] < normalized[lag]) lag++
        val period = lag + parabolicShiftOf(normalized, lag)
        return PitchFrame(seconds, sampleRate / period, 1 - normalized[lag], rms)
    }

    /** Notes an attack where the rise of the spectrum peaks above the flux of the frames before it. */
    private fun readOnset(seconds: Double, real: DoubleArray, imaginary: DoubleArray) {
        val spectrum = DoubleArray(FFT_SIZE / 2) { sqrt(real[it] * real[it] + imaginary[it] * imaginary[it]) }
        val flux = lastSpectrum?.let { last -> spectrum.indices.sumOf { max(spectrum[it] - last[it], 0.0) } } ?: 0.0
        lastSpectrum = spectrum
        fluxes.add(flux)

        // the frame before this one is an attack when it peaks above the mean of those before it
        val count = fluxes.size
        if (count < 3) return
        val peak = fluxes[count - 2]
        val recent = fluxes.subList(maxOf(0, count - 2 - ONSET_HISTORY), count - 2)
        val threshold = recent.average().takeIf { !it.isNaN() }?.let { it * ONSET_RATIO } ?: return
        if (peak > fluxes[count - 3] && peak >= fluxes[count - 1] && peak > threshold) {
            onsets.add(seconds - hopSeconds)
        }
    }
}

/** The shift from [lag] to the true dip of [values], found by fitting a parabola through its neighbours. */
private fun parabolicShiftOf(values: DoubleArray, lag: Int): Double {
    if (lag <= 0 || lag >= values.size - 1) return 0.0
    val before = values[lag - 1]
    val at = values[lag]
    val after = values[lag + 1]
    val denominator = before - 2 * at + after
    return if (abs(denominator) < 1e-12) 0.0 else 0.5 * (before - after) / denominator
}

private const val FRAME_SIZE = 2048
private const val WINDOW_SIZE = FRAME_SIZE / 2
private const val FFT_SIZE = FRAME_SIZE * 2
private const val HOP_SIZE = 480

// the range of a singing voice
private const val MIN_HERTZ = 60
private const val MAX_HERTZ = 1100

private const val YIN_THRESHOLD = 0.15
private const val ONSET_HISTORY = 10
private const val ONSET_RATIO = 1.5
