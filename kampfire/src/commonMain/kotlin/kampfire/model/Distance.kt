package kampfire.model

import kotlin.jvm.JvmInline
import kotlin.math.*

@JvmInline
value class Distance private constructor(val meters: Double) : Comparable<Distance> {
    init {
        require(meters.isFinite()) { "Distance must be finite" }
    }

    override fun compareTo(other: Distance): Int = meters.compareTo(other.meters)

    operator fun plus(other: Distance): Distance = ofMeters(meters + other.meters)
    operator fun minus(other: Distance): Distance = ofMeters(meters - other.meters)
    operator fun times(k: Double): Distance = ofMeters(meters * k)
    operator fun div(k: Double): Distance = ofMeters(meters / k)
    operator fun div(other: Distance): Double = meters / other.meters
    operator fun unaryMinus(): Distance = ofMeters(-meters)

    fun inMeters(): Double = meters
    fun inKilometers(): Double = meters / 1_000.0
    fun inFeet(): Double = meters / 0.3048
    fun inMiles(): Double = meters / 1_609.344

    fun coerceAtLeast(min: Distance): Distance = if (this < min) min else this
    fun coerceAtMost(max: Distance): Distance = if (this > max) max else this

    override fun toString(): String = when {
        abs(meters) >= 1_000.0 -> "${inKilometers()} km"
        else -> "${meters} m"
    }

    companion object {
        fun ofMeters(meters: Double): Distance = Distance(meters)
        fun ofKilometers(km: Double): Distance = Distance(km * 1_000.0)
        fun ofFeet(ft: Double): Distance = Distance(ft * 0.3048)
        fun ofMiles(mi: Double): Distance = Distance(mi * 1_609.344)
        val ZERO: Distance = Distance(0.0)
    }
}

// --- Nice unit syntax ---
val Number.meters: Distance get() = Distance.ofMeters(this.toDouble())
val Number.kilometers: Distance get() = Distance.ofKilometers(this.toDouble())
val Number.feet: Distance get() = Distance.ofFeet(this.toDouble())
val Number.miles: Distance get() = Distance.ofMiles(this.toDouble())