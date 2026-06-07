package kampfire.model

import kampfire.utils.ParameterMap
import kampfire.utils.readDoubleList
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

    fun resizeBy(factor: Float): GeoBounds {
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

fun getContainingBounds(points: List<GeoPoint>): GeoBounds? {
    if (points.size < 2) return null

    val first = points[0]
    var hasDistinct = false
    var minLng = Double.POSITIVE_INFINITY
    var maxLng = Double.NEGATIVE_INFINITY
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    for (p in points) {
        if (!hasDistinct && !p.isTouching(first)) hasDistinct = true
        if (p.lng < minLng) minLng = p.lng
        if (p.lng > maxLng) maxLng = p.lng
        if (p.lat < minLat) minLat = p.lat
        if (p.lat > maxLat) maxLat = p.lat
    }
    if (!hasDistinct) return null

    var swLng = minLng
    var neLng = maxLng

    // Naive span > 180° means the cluster may be tighter across the antimeridian.
    if (maxLng - minLng > 180.0) {
        val sorted = DoubleArray(points.size) { points[it].lng }
        sorted.sort()
        var biggestGap = 360.0 - (sorted[sorted.size - 1] - sorted[0]) // wrap-around gap
        for (i in 0 until sorted.size - 1) {
            val gap = sorted[i + 1] - sorted[i]
            if (gap > biggestGap) {
                biggestGap = gap
                swLng = sorted[i + 1]
                neLng = sorted[i]
            }
        }
    }

    return GeoBounds(
        sw = GeoPoint(lng = swLng, lat = minLat),
        ne = GeoPoint(lng = neLng, lat = maxLat),
    )
}