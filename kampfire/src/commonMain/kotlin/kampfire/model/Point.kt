package kampfire.model

import kotlin.math.sqrt

data class Point(val x: Double, val y: Double) {
    override fun toString() = "$x,$y"
}

fun Point.distanceTo(other: Point): Double {
    val dx = other.x - x
    val dy = other.y - y
    return sqrt(dx * dx + dy * dy)
}

fun Point.distanceSquaredTo(other: Point): Double {
    val dx = other.x - x
    val dy = other.y - y
    return dx * dx + dy * dy
}