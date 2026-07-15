package kampfire.utils

import kotlin.time.measureTimedValue

fun <T> printTimedValue(block: () -> T): T {
    val (value, duration) = measureTimedValue { block() }
    println(duration)
    return value
}