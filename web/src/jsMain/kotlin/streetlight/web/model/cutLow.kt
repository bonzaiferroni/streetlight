package streetlight.web.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** This sample with the frequencies below [hz] cut by a fourth-order Butterworth high-pass filter. */
fun PcmSample.cutLow(hz: Double) = PcmSample(sampleRate, channels.map { samples ->
    BUTTERWORTH_Q.fold(samples) { input, q -> input.filterHighPass(hz, q, sampleRate) }
})

// one second-order section of the filter
private fun FloatArray.filterHighPass(hz: Double, q: Double, sampleRate: Int): FloatArray {
    val omega = 2 * PI * hz / sampleRate
    val alpha = sin(omega) / (2 * q)
    val cosOmega = cos(omega)
    val a0 = 1 + alpha
    val b0 = (1 + cosOmega) / 2 / a0
    val b1 = -(1 + cosOmega) / a0
    val a1 = -2 * cosOmega / a0
    val a2 = (1 - alpha) / a0

    var input1 = 0.0
    var input2 = 0.0
    var output1 = 0.0
    var output2 = 0.0
    return FloatArray(size) { index ->
        val input = this[index].toDouble()
        val output = b0 * input + b1 * input1 + b0 * input2 - a1 * output1 - a2 * output2
        input2 = input1
        input1 = input
        output2 = output1
        output1 = output
        output.toFloat()
    }
}

// the quality of each second-order section of a fourth-order Butterworth filter
private val BUTTERWORTH_Q = listOf(0.5412, 1.3066)
