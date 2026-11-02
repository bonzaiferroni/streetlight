package streetlight.web.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Transforms [real] and [imaginary] in place by the fast Fourier transform, or by its inverse when [inverse],
 * unscaled. Both are the same size, a power of two.
 */
fun transformFourier(real: DoubleArray, imaginary: DoubleArray, inverse: Boolean = false) {
    val size = real.size

    // reorder by bit-reversed index
    var swapIndex = 0
    for (index in 1 until size) {
        var bit = size shr 1
        while (swapIndex and bit != 0) {
            swapIndex = swapIndex xor bit
            bit = bit shr 1
        }
        swapIndex = swapIndex xor bit
        if (index < swapIndex) {
            real[index] = real[swapIndex].also { real[swapIndex] = real[index] }
            imaginary[index] = imaginary[swapIndex].also { imaginary[swapIndex] = imaginary[index] }
        }
    }

    // combine butterflies of doubling length
    var length = 2
    while (length <= size) {
        val angle = (if (inverse) 2 else -2) * PI / length
        val stepReal = cos(angle)
        val stepImaginary = sin(angle)
        for (start in 0 until size step length) {
            var twiddleReal = 1.0
            var twiddleImaginary = 0.0
            for (offset in 0 until length / 2) {
                val even = start + offset
                val odd = even + length / 2
                val oddReal = real[odd] * twiddleReal - imaginary[odd] * twiddleImaginary
                val oddImaginary = real[odd] * twiddleImaginary + imaginary[odd] * twiddleReal
                real[odd] = real[even] - oddReal
                imaginary[odd] = imaginary[even] - oddImaginary
                real[even] += oddReal
                imaginary[even] += oddImaginary
                val nextReal = twiddleReal * stepReal - twiddleImaginary * stepImaginary
                twiddleImaginary = twiddleReal * stepImaginary + twiddleImaginary * stepReal
                twiddleReal = nextReal
            }
        }
        length = length shl 1
    }
}

/**
 * The impulse response that turned [stimulus] into [response], found by deconvolution, at each lag from 0 through the
 * end of [response]. Frequencies the stimulus barely holds are left out rather than amplified.
 */
fun impulseResponseOf(response: FloatArray, stimulus: FloatArray): DoubleArray {
    var size = 1
    while (size < response.size + stimulus.size) size = size shl 1

    val responseReal = DoubleArray(size) { if (it < response.size) response[it].toDouble() else 0.0 }
    val responseImaginary = DoubleArray(size)
    val stimulusReal = DoubleArray(size) { if (it < stimulus.size) stimulus[it].toDouble() else 0.0 }
    val stimulusImaginary = DoubleArray(size)
    transformFourier(responseReal, responseImaginary)
    transformFourier(stimulusReal, stimulusImaginary)

    // divide the response's spectrum by the stimulus's, regularized below its peak power
    var peakPower = 0.0
    for (index in 0 until size) {
        peakPower = maxOf(peakPower, stimulusReal[index] * stimulusReal[index] + stimulusImaginary[index] * stimulusImaginary[index])
    }
    val floorPower = peakPower * REGULARIZATION
    for (index in 0 until size) {
        val power = stimulusReal[index] * stimulusReal[index] + stimulusImaginary[index] * stimulusImaginary[index]
        val real = responseReal[index] * stimulusReal[index] + responseImaginary[index] * stimulusImaginary[index]
        val imaginary = responseImaginary[index] * stimulusReal[index] - responseReal[index] * stimulusImaginary[index]
        responseReal[index] = real / (power + floorPower)
        responseImaginary[index] = imaginary / (power + floorPower)
    }

    transformFourier(responseReal, responseImaginary, inverse = true)
    return DoubleArray(response.size) { responseReal[it] / size }
}

// 50 dB below the stimulus's peak power
private const val REGULARIZATION = 1e-5
