package streetlight.compose.svg

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

/**
 * When an animation is at a time: repeating every [duration] seconds from [begin], through keyframes at [keyTimes],
 * each step eased by its spline from [splines], or held when [isDiscrete].
 */
internal class SvgTiming(
    val duration: Float,
    val begin: Float,
    val keyTimes: List<Float>?,
    val splines: List<Easing>?,
    val isDiscrete: Boolean,
) {
    /** The keyframe at [time] among [count] keyframes, and the eased fraction of the way to the next. */
    fun keyframeAt(time: Float, count: Int): Pair<Int, Float> {
        if (count < 2 || duration <= 0f) return 0 to 0f
        val local = ((time - begin) % duration + duration) % duration / duration
        val times = keyTimes?.takeIf { it.size == count } ?: List(count) { it / (count - 1f) }

        val index = (times.indexOfLast { it <= local }).coerceIn(0, count - 1)
        if (isDiscrete || index == count - 1) return index to 0f

        val span = times[index + 1] - times[index]
        val fraction = if (span <= 0f) 0f else (local - times[index]) / span
        val easing = splines?.getOrNull(index)
        return index to (easing?.transform(fraction) ?: fraction)
    }
}

/** The timing an animation element declares with `dur`, `begin`, `keyTimes`, `keySplines` and `calcMode`. */
internal fun timingOf(attributes: Map<String, String>) = SvgTiming(
    duration = parseClock(attributes["dur"]) ?: 0f,
    begin = parseClock(attributes["begin"]) ?: 0f,
    keyTimes = attributes["keyTimes"]?.split(';')?.mapNotNull { it.trim().toFloatOrNull() },
    splines = attributes["keySplines"]?.takeIf { attributes["calcMode"] == "spline" }?.split(';')?.mapNotNull { spline ->
        val points = numbersOf(spline)
        if (points.size == 4) CubicBezierEasing(points[0], points[1], points[2], points[3]) else null
    },
    isDiscrete = attributes["calcMode"] == "discrete",
)

/** A clock value such as `2s`, `-0.3s` or `500ms`, in seconds. */
private fun parseClock(value: String?): Float? {
    val text = value?.trim() ?: return null
    return when {
        text.endsWith("ms") -> text.removeSuffix("ms").toFloatOrNull()?.div(1000f)
        text.endsWith("s") -> text.removeSuffix("s").toFloatOrNull()
        else -> text.toFloatOrNull()
    }
}
