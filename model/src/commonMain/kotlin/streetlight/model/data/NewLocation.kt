package streetlight.model.data

import kabinet.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class NewLocation(
    val areaId: Int,
    val name: String?,
    val geoPoint: GeoPoint,
)