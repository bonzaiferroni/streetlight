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

/** The cross-correlation of [signal] with [kernel] at each lag from 0 through the end of [signal]. */
fun crossCorrelationOf(signal: FloatArray, kernel: FloatArray): DoubleArray {
    var size = 1
    while (size < signal.size + kernel.size) size = size shl 1

    val signalReal = DoubleArray(size) { if (it < signal.size) signal[it].toDouble() else 0.0 }
    val signalImaginary = DoubleArray(size)
    val kernelReal = DoubleArray(size) { if (it < kernel.size) kernel[it].toDouble() else 0.0 }
    val kernelImaginary = DoubleArray(size)
    transformFourier(signalReal, signalImaginary)
    transformFourier(kernelReal, kernelImaginary)

    // multiply the signal's spectrum by the kernel's conjugate
    for (index in 0 until size) {
        val real = signalReal[index] * kernelReal[index] + signalImaginary[index] * kernelImaginary[index]
        val imaginary = signalImaginary[index] * kernelReal[index] - signalReal[index] * kernelImaginary[index]
        signalReal[index] = real
        signalImaginary[index] = imaginary
    }

    transformFourier(signalReal, signalImaginary, inverse = true)
    return DoubleArray(signal.size) { signalReal[it] / size }
}
