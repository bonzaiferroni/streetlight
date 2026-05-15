package streetlight.model.data

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.utils.ParameterMap
import kampfire.utils.readFloat
import kotlinx.serialization.Serializable

@Serializable
data class MapQuery(
    val bounds: GeoBounds,
    val zoom: Float,
) {
    fun toQuery() = "${bounds.toQuery()}&$ZOOM_KEY=$zoom"

    fun contains(point: GeoPoint) = point.lng >= bounds.sw.lng && point.lng < bounds.ne.lng
            && point.lat >= bounds.sw.lat && point.lat < bounds.ne.lat

    companion object {
        const val ZOOM_KEY = "zoom"

        fun fromQuery(parameters: ParameterMap): MapQuery {
            val bounds = GeoBounds.fromQuery(parameters) ?: error("bounds not found")
            val zoom = parameters.readFloat(ZOOM_KEY) ?: error("zoom not found")
            return MapQuery(bounds, zoom)
        }
    }
}