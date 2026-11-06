package streetlight.web.model

import kotlin.math.max

/** The stretch of the timeline the lanes show, [spanSeconds] long from [startSeconds]. */
data class TimeWindow(
    val startSeconds: Double,
    val spanSeconds: Double,
) {
    val endSeconds get() = startSeconds + spanSeconds

    /** Where [seconds] falls across the window, from 0 at its start to 1 at its end. */
    fun toRatio(seconds: Double) = (seconds - startSeconds) / spanSeconds

    /** The seconds at [ratio] across the window, the inverse of [toRatio]. */
    fun toSeconds(ratio: Double) = startSeconds + ratio * spanSeconds

    /** This window [spanSeconds] long from the same start, no shorter than four seconds and within [totalSeconds]. */
    fun resize(spanSeconds: Double, totalSeconds: Double) =
        copy(spanSeconds = spanSeconds.coerceIn(MIN_SPAN_SECONDS, max(totalSeconds - startSeconds, MIN_SPAN_SECONDS)))

    /** This window moved as little as it can to show [seconds], keeping a fifth of its span ahead when it advances. */
    fun follow(seconds: Double) = when {
        seconds < startSeconds -> copy(startSeconds = seconds)
        seconds > endSeconds -> copy(startSeconds = seconds - spanSeconds * (1 - LEAD_SHARE))
        else -> this
    }
}

private const val LEAD_SHARE = 0.2
private const val MIN_SPAN_SECONDS = 4.0
