package streetlight.web.model

import kotlin.math.max
import kotlin.math.min

/**
 * The low and high peaks of a sample across its channels, summarized at several block sizes, each four times the
 * last. Reading peaks through the summary costs about one step per column rather than one per frame.
 */
class PeakSummary private constructor(private val levels: List<PeakLevel>) {

    /**
     * The peaks of each of [columnCount] even spans of the frames from [startFrame] until [endFrame], as consecutive
     * low and high pairs, or `null` when the columns are too narrow for any level, so the frames are read instead.
     */
    fun readPeaks(startFrame: Int, endFrame: Int, columnCount: Int): FloatArray? {
        val framesPerColumn = (endFrame - startFrame).toDouble() / columnCount
        val level = levels.lastOrNull { it.blockFrames <= framesPerColumn } ?: return null

        val peaks = FloatArray(columnCount * 2)
        for (column in 0 until columnCount) {
            val startBlock = ((startFrame + column * framesPerColumn) / level.blockFrames).toInt()
            val endBlock = ((startFrame + (column + 1) * framesPerColumn) / level.blockFrames).toInt()
            var low = 0f
            var high = 0f
            for (block in startBlock until min(max(endBlock, startBlock + 1), level.blockCount)) {
                low = min(low, level.lows[block])
                high = max(high, level.highs[block])
            }
            peaks[column * 2] = low
            peaks[column * 2 + 1] = high
        }
        return peaks
    }

    companion object {
        /** The summary of [channels], each level built from the one below it. */
        fun of(channels: List<FloatArray>): PeakSummary {
            val frameCount = channels.firstOrNull()?.size ?: 0
            val levels = mutableListOf<PeakLevel>()

            // summarize the frames into the smallest blocks
            val firstCount = (frameCount + FIRST_BLOCK_FRAMES - 1) / FIRST_BLOCK_FRAMES
            val firstLows = FloatArray(firstCount)
            val firstHighs = FloatArray(firstCount)
            channels.forEach { samples ->
                for (frame in samples.indices) {
                    val block = frame / FIRST_BLOCK_FRAMES
                    firstLows[block] = min(firstLows[block], samples[frame])
                    firstHighs[block] = max(firstHighs[block], samples[frame])
                }
            }
            levels.add(PeakLevel(FIRST_BLOCK_FRAMES, firstLows, firstHighs))

            // summarize each level into the next, until a level holds a single block
            while (levels.last().blockCount > 1) {
                val below = levels.last()
                val count = (below.blockCount + LEVEL_FACTOR - 1) / LEVEL_FACTOR
                val lows = FloatArray(count)
                val highs = FloatArray(count)
                for (block in 0 until below.blockCount) {
                    lows[block / LEVEL_FACTOR] = min(lows[block / LEVEL_FACTOR], below.lows[block])
                    highs[block / LEVEL_FACTOR] = max(highs[block / LEVEL_FACTOR], below.highs[block])
                }
                levels.add(PeakLevel(below.blockFrames * LEVEL_FACTOR, lows, highs))
            }
            return PeakSummary(levels)
        }
    }
}

// one level of the summary, the peaks of each block of blockFrames frames
private class PeakLevel(val blockFrames: Int, val lows: FloatArray, val highs: FloatArray) {
    val blockCount get() = lows.size
}

private const val FIRST_BLOCK_FRAMES = 256
private const val LEVEL_FACTOR = 4
