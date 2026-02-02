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

    fun distanceTo(other: GeoPoint): Double {
        val earthRadiusMeters = 6_371_000.0
        val degToRad = PI / 180.0

        val lat1 = lat * degToRad
        val lat2 = other.lat * degToRad
        val dLat = (other.lat - lat) * degToRad
        val dLng = (other.lng - lng) * degToRad

        val a =
            sin(dLat / 2).pow(2) +
                    cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadiusMeters * c
    }

    fun toQuery() = "lng=$lng&lat=$lat"

    companion object {
        val Denver = GeoPoint(-104.95, 39.75)

        fun fromQuery(parameters: ParameterMap) = parameters.let {
            val lng = parameters["lng"]?.firstOrNull()?.toDoubleOrNull() ?: return@let null
            val lat = parameters["lat"]?.firstOrNull()?.toDoubleOrNull() ?: return@let null
            GeoPoint(lng, lat)
        }
    }
}