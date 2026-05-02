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

    companion object {
        val Denver = GeoPoint(-104.95, 39.75)

        fun fromQuery(parameters: ParameterMap) = parameters.let {
            val lng = parameters["lng"]?.firstOrNull()?.toDoubleOrNull() ?: return@let null
            val lat = parameters["lat"]?.firstOrNull()?.toDoubleOrNull() ?: return@let null
            GeoPoint(lng, lat)
        }

        fun fromString(value: String) = value.split(",").mapNotNull { it.toDoubleOrNull() }.takeIf { it.size == 2 }?.let {
            GeoPoint(it[0], it[1])
        }
    }
}

fun GeoPoint.distanceTo(other: GeoPoint): Distance {
    val lat1 = lat * degToRad
    val lat2 = other.lat * degToRad
    val dLat = (other.lat - lat) * degToRad
    val dLng = (other.lng - lng) * degToRad

    val a =
        sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return Distance.ofMeters(earthRadius * c)
}

fun GeoPoint.regionalDistanceTo(other: GeoPoint): Distance {
    val lat1 = lat * degToRad
    val lat2 = other.lat * degToRad
    val dLat = (other.lat - lat) * degToRad
    val dLng = (other.lng - lng) * degToRad

    val x = dLng * cos((lat1 + lat2) * 0.5)
    val y = dLat
    return Distance.ofMeters(earthRadius * sqrt(x * x + y * y))
}

fun GeoPoint.toPoint(refLat: Double): Point {
    val latRad = lat * degToRad
    val lngRad = lng * degToRad
    val refLatRad = refLat * degToRad

    val x = earthRadius * lngRad * cos(refLatRad)
    val y = earthRadius * latRad

    return Point(x, y)
}

const val earthRadius = 6_371_000.0
const val degToRad = PI / 180.0