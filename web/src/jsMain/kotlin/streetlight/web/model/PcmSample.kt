package streetlight.web.model

import kotlin.math.max

/** Uncompressed audio at [sampleRate], one array of samples between -1 and 1 per channel in [channels]. */
class PcmSample(
    val sampleRate: Int,
    val channels: List<FloatArray>,
) {
    val frameCount get() = channels.firstOrNull()?.size ?: 0
    val durationSeconds get() = frameCount.toDouble() / sampleRate

    /**
     * The lowest and highest sample across all channels in each of [columnCount] even spans of the sample, as
     * consecutive low and high pairs.
     */
    fun readPeaks(columnCount: Int): FloatArray {
        val peaks = FloatArray(columnCount * 2)
        if (frameCount == 0) return peaks

        for (column in 0 until columnCount) {
            val start = (column.toLong() * frameCount / columnCount).toInt()
            val end = max(((column + 1).toLong() * frameCount / columnCount).toInt(), start + 1)
            var low = 0f
            var high = 0f
            channels.forEach { samples ->
                for (frame in start until end) {
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
