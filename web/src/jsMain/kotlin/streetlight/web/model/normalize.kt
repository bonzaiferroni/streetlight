package streetlight.web.model

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This sample brought to the target loudness, measured over the blocks that hold sound rather than silence, unless
 * that would raise its peak above the ceiling.
 */
fun PcmSample.normalize(): PcmSample {
    val blockSize = (BLOCK_SECONDS * sampleRate).toInt()
    if (frameCount < blockSize) return this

    // measure the power of each block, across channels
    val blockPowers = (0 until frameCount / blockSize).map { block ->
        val start = block * blockSize
        channels.sumOf { samples -> (start until start + blockSize).sumOf { samples[it].toDouble() * samples[it] } } /
            (blockSize * channels.size)
    }

    // gate out silence, then quiet passages, and average the rest
    val audible = blockPowers.filter { it > ABSOLUTE_GATE_POWER }
    if (audible.isEmpty()) return this
    val relativeGate = audible.average() * RELATIVE_GATE
    val loudness = sqrt(audible.filter { it > relativeGate }.average())

    val peak = channels.maxOf { samples -> samples.maxOf { abs(it) } }
    val gain = min(TARGET_LEVEL / loudness, CEILING_LEVEL / peak).toFloat()
    return PcmSample(sampleRate, channels.map { samples -> FloatArray(samples.size) { samples[it] * gain } })
}

private const val BLOCK_SECONDS = 0.1
private val TARGET_LEVEL = 10.0.pow(-16.0 / 20)
private val CEILING_LEVEL = 10.0.pow(-1.0 / 20)
// -70 dB, and 10 dB below the audible average
private val ABSOLUTE_GATE_POWER = 10.0.pow(-70.0 / 10)
private val RELATIVE_GATE = 10.0.pow(-10.0 / 10)
