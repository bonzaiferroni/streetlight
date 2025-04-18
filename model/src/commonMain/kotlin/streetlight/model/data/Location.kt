package streetlight.model.data

import kabinet.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: Int,
    val userId: Long?,
    val areaId: Int?,
    val name: String?,
    val description: String?,
    val address: String?,
    val notes: String?,
    val geoPoint: GeoPoint,
    val resources: Set<ResourceType>,
)