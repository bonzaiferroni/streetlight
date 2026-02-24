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
    val imageUrl: String?,
    val thumbUrl: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class LocationId(override val value: String): ProjectId {
    companion object { fun random() = LocationId(randomUuidString())}
}

data class LocationEdit(
    val locationId: LocationId? = null,
    val name: String = "",
    val description: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val geoPoint: GeoPoint = GeoPoint.Denver,
    val resources: Set<ResourceType> = emptySet(),
)

@Serializable
data class Place(
    val name: String? = null,
    val address: String? = null,
    val geoPoint: GeoPoint? = null,
) {
    val isValid get() = !name.isNullOrBlank() && geoPoint != null
}

fun Location.toEdit() = LocationEdit(
    locationId = locationId,
    name = name,
    description = description,
    address = address,
    notes = notes,
    geoPoint = geoPoint,
    resources = resources
)

fun Location.toPlace() = Place(
    name = name,
    address = address,
    geoPoint = geoPoint,
)

fun LocationEdit.toPlace() = Place(
    name = name,
    address = address,
    geoPoint = geoPoint,
)

fun Place.toLocation(
    userId: UserId? = null
) = Location(
    locationId = LocationId.random(),
    hostId = userId,
    name = name ?: "",
    geoPoint = geoPoint ?: GeoPoint.Denver,
    description = null,
    address = address,
    notes = null,
    resources = emptySet(),
    imageUrl = null,
    thumbUrl = null,
    updatedAt = Clock.System.now(),
    createdAt = Clock.System.now()
)