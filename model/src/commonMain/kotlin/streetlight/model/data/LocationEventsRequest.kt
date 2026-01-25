package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class LocationEventsRequest(
    val point: GeoPoint,
    val zoom: Float,
) {
    fun toQuery() = "${point.toQuery()}&zoom=$zoom"

    companion object {
        fun fromQuery(parameters: Map<String, List<String>>) = parameters.let {
            val zoom = parameters["zoom"]?.firstOrNull()?.toFloatOrNull() ?: return@let null
            val point = GeoPoint.Companion.fromQuery(parameters) ?: return@let null
            LocationEventsRequest(point, zoom)
        }
    }
}