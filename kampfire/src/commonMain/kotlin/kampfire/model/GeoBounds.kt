package kampfire.model

import kampfire.utils.ParameterMap
import kampfire.utils.readDoubleList
import kampfire.utils.readFloat
import kampfire.utils.readFloatList
import kotlinx.serialization.Serializable

@Serializable
data class GeoBounds(
    val sw: GeoPoint,
    val ne: GeoPoint
) {
    fun toQuery() = "$QUERY_KEY=${sw.lng}&$QUERY_KEY=${sw.lat}&$QUERY_KEY=${ne.lng}&$QUERY_KEY=${ne.lat}"

    val center get() = GeoPoint(
        lng = (sw.lng + ne.lng) / 2.0,
        lat = (sw.lat + ne.lat) / 2.0
    )

    fun contains(point: GeoPoint) = point.lng >= sw.lng && point.lng < ne.lng
            && point.lat >= sw.lat && point.lat < ne.lat

    fun contains(bounds: GeoBounds) = bounds.sw.lng >= sw.lng && bounds.ne.lng < ne.lng
            && bounds.sw.lat >= sw.lat && bounds.ne.lat < ne.lat

    fun expandBy(factor: Float): GeoBounds {
        val center = center
        val halfWidth = (ne.lng - sw.lng) / 2.0 * factor
        val halfHeight = (ne.lat - sw.lat) / 2.0 * factor
        return GeoBounds(
            sw = GeoPoint(center.lng - halfWidth, center.lat - halfHeight),
            ne = GeoPoint(center.lng + halfWidth, center.lat + halfHeight)
        )
    }

    companion object {
        const val QUERY_KEY = "bounds"

        val Denver = GeoBounds(GeoPoint(-105.05, 39.75), GeoPoint(-104.85, 39.95))

        fun fromQuery(parameters: ParameterMap): GeoBounds? {
            val bounds = parameters.readDoubleList(QUERY_KEY)?.takeIf { it.size == 4 } ?: return null
            return GeoBounds(GeoPoint(bounds[0], bounds[1]), GeoPoint(bounds[2], bounds[3]))
        }
    }
}