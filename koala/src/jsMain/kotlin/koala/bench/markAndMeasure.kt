package koala.bench

import web.performance.performance

/**
 * Runs [block] and records its duration as a performance measure named [label].
 *
 * The measure shows in the Timings track of a browser Performance profile.
 */
inline fun <T> markAndMeasure(label: String, block: () -> T): T {
    val startMark = "$label:start"
    val endMark = "$label:end"
    performance.mark(startMark)
    try {
        return block()
    } finally {
        performance.mark(endMark)
        performance.measure(label, startMark, endMark)
    }
}
