package kampfire.model

import kampfire.utils.ParameterMap
import kotlinx.serialization.Serializable
import kotlin.math.*

@Serializable
data class GeoPoint(
    val lng: Double,
    val lat: Double,
) {
    val x get() = lng
    val y get() = lat

    fun toList(): List<Double> = listOf(lng, lat)
    fun toArray(): Array<Double> = arrayOf(lng, lat)

    override fun toString() = "$lng,$lat"

    fun toQuery() = "lng=$lng&lat=$lat"

    fun isTouching(other: GeoPoint) = abs(lng - other.lng) < 1e-9 && abs(lat - other.lat) < 1e-9

    companion object {
        val Denver = GeoPoint(-104.95, 39.75)

        fun fromQuery(parameters: ParameterMap) = parameters.let {
            val lng = parameters["lng"]?.firstOrNull()?.toDoubleOrNull() ?: error("lng not found")
            val lat = parameters["lat"]?.firstOrNull()?.toDoubleOrNull() ?: error("lat not found")
            GeoPoint(lng, lat)
        }

        fun fromString(value: String) = value.split(",").mapNotNull { it.toDoubleOrNull() }.takeIf { it.size == 2 }?.let {
            GeoPoint(it[0], it[1])
        }
    }
}

fun GeoPoint.distanceTo(other: GeoPoint): Distance {
    val lat1 = lat * DEG_TO_RAD
    val lat2 = other.lat * DEG_TO_RAD
    val dLat = (other.lat - lat) * DEG_TO_RAD
    val dLng = (other.lng - lng) * DEG_TO_RAD

    val a =
        sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return Distance.ofMeters(EARTH_RADIUS * c)
}

fun GeoPoint.regionalDistanceTo(other: GeoPoint): Distance {
    val lat1 = lat * DEG_TO_RAD
    val lat2 = other.lat * DEG_TO_RAD
    val dLat = (other.lat - lat) * DEG_TO_RAD
    val dLng = (other.lng - lng) * DEG_TO_RAD

    val x = dLng * cos((lat1 + lat2) * 0.5)
    val y = dLat
    return Distance.ofMeters(EARTH_RADIUS * sqrt(x * x + y * y))
}

fun GeoPoint.toPlanarPoint(refLat: Double): Point {
    val latRad = lat * DEG_TO_RAD
    val lngRad = lng * DEG_TO_RAD
    val refLatRad = refLat * DEG_TO_RAD

    val x = EARTH_RADIUS * lngRad * cos(refLatRad)
    val y = EARTH_RADIUS * latRad

    return Point(x, y)
}

const val EARTH_RADIUS = 6_371_000.0
const val DEG_TO_RAD = PI / 180.0