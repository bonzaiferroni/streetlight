package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.UserId
import kampfire.utils.randomUuidString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Location(
    val locationId: LocationId,
    val hostId: UserId?,
    val name: String,
    val description: String?,
    val address: String?,
    val notes: String?,
    val geoPoint: GeoPoint,
    val resources: Set<ResourceType>,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class LocationId(override val value: String): ProjectId {
    companion object { fun random() = LocationId(randomUuidString())}
}

@Serializable
data class NewLocation(
    val name: String = "",
    val address: String? = null,
    val geoPoint: GeoPoint = GeoPoint.Denver
) {
    val isValid get() = name.isNotBlank() && geoPoint != GeoPoint.Denver // sry Denver

    fun toLocation() = Location(
        locationId = LocationId.random(),
        hostId = null,
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