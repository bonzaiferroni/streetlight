package streetlight.model.data

import kampfire.model.GeoPoint
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
    // td: addressNumber
    // td: street
    // td: unit/suite
    // td: postalCode
    // td: state
    // td: country
    val geoPoint: GeoPoint,
    val resources: Set<ResourceType>,
    // td: rename -> website
    val link: String?,
    // td: rename -> calendar
    val eventsLink: String?,
    // td: create -> menu
    // td: create -> tags
    // td: aboutLink
    val imageUrl: String?,
    val thumbUrl: String?,
    val updatedAt: Instant,
    val createdAt: Instant,
    // td: openedAt: LocalDate
    // td: LocationTags
)

@JvmInline @Serializable
value class LocationId(override val value: String): ProjectId {
    companion object { fun random() = LocationId(randomUuidString())}
}

@Serializable
data class LocationEdit(
    val locationId: LocationId? = null,
    val name: String? = null,
    val description: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val geoPoint: GeoPoint? = null,
    val resources: Set<ResourceType>? = null,
    val website: String? = null,
    val eventsLink: String? = null,
    val imageUrl: String? = null,
    val thumbUrl: String? = null,
    val isHost: Boolean = false,
) {
    val isValid get() = name != null && geoPoint != null
}

@Serializable
data class LocationAddress(
    val streetAddress: String,
    val postCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
)

@Serializable
data class Place(
    val name: String? = null,
    val address: String? = null,
    val postalCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    val geoPoint: GeoPoint? = null,
    val website: String? = null,
) {
    val isValid get() = !name.isNullOrBlank() && geoPoint != null
}

fun Location.toEdit() = LocationEdit(
    locationId = locationId,
    name = name,
    description = description,
    address = address,
    geoPoint = geoPoint,
    resources = resources,
    website = link,
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
    resources = emptySet(),
    link = null,
    eventsLink = null,
    imageUrl = null,
    thumbUrl = null,
    updatedAt = Clock.System.now(),
    createdAt = Clock.System.now()
)

fun Place.toEdit() = LocationEdit(
    name = name,
    address = address,
    geoPoint = geoPoint,
    website = website
)

fun LocationEdit.toLocation() = Location(
    locationId = locationId ?: LocationId.random(),
    name = name ?: error("no location name"),
    geoPoint = geoPoint ?: error("no location geoPoint"),
    description = description,
    address = address,
    resources = resources ?: emptySet(),
    link = website,
    eventsLink = eventsLink,
    imageUrl = imageUrl,
    thumbUrl = thumbUrl,
    updatedAt = Clock.System.now(),
    createdAt = Clock.System.now()
)

fun LocationParse.toEdit(
    locationId: LocationId? = null
) = LocationEdit(
    locationId = locationId,
    name = name ?: "",
    description = description,
    website = url,
    eventsLink = eventsUrl,
    imageUrl = imageUrl,
)

fun LocationParse.toAddress() = address?.let {
    LocationAddress(
        streetAddress = it,
        postCode = postalCode,
        city = city,
        state = state,
        country = country,
    )
}

fun LocationEdit.mergeLeft(edit: LocationEdit?) = edit?.let {
    LocationEdit(
        locationId = locationId ?: edit.locationId,
        name = name ?: edit.name,
        description = description ?: edit.description,
        address = address ?: edit.address,
        geoPoint = geoPoint ?: edit.geoPoint,
        resources = resources ?: edit.resources,
        website = website ?: edit.website,
        eventsLink = eventsLink ?: edit.eventsLink,
        imageUrl = imageUrl ?: edit.imageUrl,
        thumbUrl = thumbUrl ?: edit.thumbUrl,
    )
} ?: this

fun LocationEdit.mergeRight(edit: LocationEdit?) = edit?.mergeLeft(this) ?: this