package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.UserId
import kampfire.utils.ParseHint
import kampfire.utils.randomUuidString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Location(
    val locationId: LocationId,
    val name: String,
    val description: String?,
    val address: String?,
    val notes: String?,
    val geoPoint: GeoPoint,
    val resources: Set<ResourceType>,
    val link: String?,
    val eventsLink: String?,
    val imageUrl: String?,
    val thumbUrl: String?,
    val checkedAt: Instant?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline @Serializable
value class LocationId(override val value: String): ProjectId {
    companion object { fun random() = LocationId(randomUuidString())}
}

@Serializable
data class LocationEdit(
    val locationId: LocationId? = null,
    val name: String = "",
    val description: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val geoPoint: GeoPoint = GeoPoint.Denver,
    val resources: Set<ResourceType> = emptySet(),
    val link: String? = null,
    val eventsLink: String? = null,
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
    val isHost: Boolean = false,
)

@Serializable
data class LocationParse(
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val url: String? = null,
    val eventsUrl: String? = null,
    val imageUrl: String? = null,
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
    resources = resources,
    link = link,
    eventsLink = eventsLink,
    imageUrl = imageUrl,
    thumbUrl = thumbUrl,
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

fun Place.toLocation() = Location(
    locationId = LocationId.random(),
    name = name ?: "",
    geoPoint = geoPoint ?: GeoPoint.Denver,
    description = null,
    address = address,
    notes = null,
    resources = emptySet(),
    link = null,
    eventsLink = null,
    imageUrl = null,
    thumbUrl = null,
    checkedAt = null,
    updatedAt = Clock.System.now(),
    createdAt = Clock.System.now()
)

fun LocationEdit.toLocation() = Location(
    locationId = locationId ?: LocationId.random(),
    name = name,
    geoPoint = geoPoint,
    description = description,
    address = address,
    notes = notes,
    resources = resources,
    link = link,
    eventsLink = eventsLink,
    imageUrl = imageUrl,
    thumbUrl = thumbUrl,
    checkedAt = null,
    updatedAt = Clock.System.now(),
    createdAt = Clock.System.now()
)

fun LocationParse.toLocationEdit(
    locationId: LocationId? = null
) = LocationEdit(
    locationId = locationId,
    name = name ?: "",
    description = description,
    address = address,
    link = url,
    eventsLink = eventsUrl,
    imageUrl = imageUrl,
)