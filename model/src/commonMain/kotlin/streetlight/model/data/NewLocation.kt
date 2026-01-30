package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class NewLocation(
    val areaId: AreaId,
    val name: String,
    val geoPoint: GeoPoint,
) {
    fun toLocation() = Location(
        locationId = LocationId.random(),
        userId = null,
        areaId = areaId,
        name = name,
        geoPoint = geoPoint,
        description = null,
        address = null,
        notes = null,
        resources = emptySet(),
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
}