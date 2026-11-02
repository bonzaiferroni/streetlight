package streetlight.web.model

import kotlin.math.max
import kotlin.math.min

/** Uncompressed audio at [sampleRate], one array of samples between -1 and 1 per channel in [channels]. */
class PcmSample(
    val sampleRate: Int,
    val channels: List<FloatArray>,
) {
    val frameCount get() = channels.firstOrNull()?.size ?: 0
    val durationSeconds get() = frameCount.toDouble() / sampleRate

    private val summary by lazy { PeakSummary.of(channels) }

    /**
     * The lowest and highest sample across all channels in each of [columnCount] even spans of the frames from
     * [startFrame] until [endFrame], as consecutive low and high pairs. Columns wide enough are read from a summary
     * built on first use, and narrower ones from the frames.
     */
    fun readPeaks(startFrame: Int, endFrame: Int, columnCount: Int): FloatArray {
        val rangeFrames = endFrame - startFrame
        if (rangeFrames <= 0) return FloatArray(columnCount * 2)
        summary.readPeaks(startFrame, endFrame, columnCount)?.let { return it }

        val peaks = FloatArray(columnCount * 2)

        for (column in 0 until columnCount) {
            val start = startFrame + (column.toLong() * rangeFrames / columnCount).toInt()
            val end = max(startFrame + ((column + 1).toLong() * rangeFrames / columnCount).toInt(), start + 1)
            var low = 0f
            var high = 0f
            channels.forEach { samples ->
                for (frame in start until min(end, samples.size)) {
                    val value = samples[frame]
                    if (value < low) low = value
                    if (value > high) high = value
                }
            }
            peaks[column * 2] = low
            peaks[column * 2 + 1] = high
        }
        return peaks
    }
}
