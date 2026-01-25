package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class NewLocation(
    val areaId: AreaId,
    val name: String,
    val geoPoint: GeoPoint,
)