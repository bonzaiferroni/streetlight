package kampfire.utils

import kotlin.time.measureTimedValue

/** Runs [block], prints how long it took, and returns its value. */
fun <T> printTimedValue(block: () -> T): T {
    val (value, duration) = measureTimedValue { block() }
    println(duration)
    return value
}