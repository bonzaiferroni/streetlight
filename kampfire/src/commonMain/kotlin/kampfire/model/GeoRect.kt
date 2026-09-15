package kampfire.model

import kampfire.utils.ParameterMap
import kampfire.utils.readDoubleList
import kotlinx.serialization.Serializable

@Serializable
data class GeoRect(
    val sw: GeoPoint,
    val ne: GeoPoint
) {
    @Deprecated("use endpoint parser")
    fun toQuery() = "$QUERY_KEY=${sw.lng}&$QUERY_KEY=${sw.lat}&$QUERY_KEY=${ne.lng}&$QUERY_KEY=${ne.lat}"

    val west get() = sw.lng
    val east get() = ne.lng
    val north get() = ne.lat
    val south get() = sw.lat
    val width get() = east - west
    val height get() = north - south
    val center get() = GeoPoint((west + east) / 2.0, (south + north) / 2.0)

    fun contains(point: GeoPoint) = west <= point.lng && east >= point.lng &&
            south <= point.lat && north >= point.lat

    fun contains(other: GeoRect) = west <= other.west && east >= other.east &&
            south <= other.south && north >= other.north

    fun overlapArea(other: GeoRect): Double =
        maxOf(0.0, minOf(east, other.east) - maxOf(west, other.west)) *
                maxOf(0.0, minOf(north, other.north) - maxOf(south, other.south))

    fun scaleBy(factor: Float): GeoRect {
        val center = center
        val halfWidth = width / 2.0 * factor
        val halfHeight = height / 2.0 * factor
        return GeoRect(
            sw = GeoPoint(center.lng - halfWidth, center.lat - halfHeight),
            ne = GeoPoint(center.lng + halfWidth, center.lat + halfHeight)
        )
    }

    override fun toString() = "$sw:$ne"

    companion object {
        const val QUERY_KEY = "bounds"

        val Denver = GeoRect(GeoPoint(-105.05, 39.75), GeoPoint(-104.85, 39.95))

        @Deprecated("use endpoint parser")
        fun fromQuery(parameters: ParameterMap): GeoRect? {
            val bounds = parameters.readDoubleList(QUERY_KEY)?.takeIf { it.size == 4 } ?: return null
            return GeoRect(GeoPoint(bounds[0], bounds[1]), GeoPoint(bounds[2], bounds[3]))
        }

        fun of(value: String): GeoRect? = value.split(":").mapNotNull { GeoPoint.of(it) }
            .takeIf { it.size == 2 }?.let { GeoRect(it[0], it[1]) }

        fun arrayOf(value: String): List<GeoRect>? = value.split("|").map { of(it) }
            .takeIf { bounds -> bounds.all { it != null } }?.filterNotNull()
    }
}

fun List<GeoRect>.toArrayString(): String = joinToString("|") { it.toString() }

fun getContainingBounds(points: List<GeoPoint>): GeoRect? {
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

    return GeoRect(
        sw = GeoPoint(lng = swLng, lat = minLat),
        ne = GeoPoint(lng = neLng, lat = maxLat),
    )
}