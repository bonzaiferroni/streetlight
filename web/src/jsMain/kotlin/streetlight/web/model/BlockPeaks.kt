package streetlight.web.model

import kotlin.math.max
import kotlin.math.min

/** The low and high peak of each block of a capture in progress, across its channels, added as blocks arrive. */
class BlockPeaks(val sampleRate: Int) {
    private var values = FloatArray(INITIAL_CAPACITY * 2)

    var blockCount = 0
        private set

    val durationSeconds get() = blockCount.toDouble() * BLOCK_FRAMES / sampleRate

    /** Adds the peaks of [block], one array of samples per channel. */
    fun add(block: Array<FloatArray>) {
        if (blockCount * 2 == values.size) values = values.copyOf(values.size * 2)
        var low = 0f
        var high = 0f
        block.forEach { samples ->
            for (frame in samples.indices) {
                low = min(low, samples[frame])
                high = max(high, samples[frame])
            }
        }
        values[blockCount * 2] = low
        values[blockCount * 2 + 1] = high
        blockCount++
    }

    fun readLow(block: Int) = values[block * 2]
    fun readHigh(block: Int) = values[block * 2 + 1]
}

/** The frames in each block a capture delivers, the render quantum of Web Audio. */
const val BLOCK_FRAMES = 128

private const val INITIAL_CAPACITY = 4096
