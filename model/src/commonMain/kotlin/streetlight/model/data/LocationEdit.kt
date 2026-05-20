package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable
import streetlight.model.external.OSMLocation
import streetlight.model.external.toGeoPoint

@Serializable
data class LocationEdit(
    val locationId: LocationId? = null,
    val cityId: CityId? = null,
    val name: String? = null,
    val city: String? = null,
    val description: String? = null,
    val address: String? = null,
    val state: String? = null,
    val country: String? = null,
    val notes: String? = null,
    val geoPoint: GeoPoint? = null,
    val mapId: MapId? = null,
    val mapRank: Float? = null,
    val mapCategory: String? = null,
    val mapType: String? = null,
    val resources: Set<ResourceType>? = null,
    val website: String? = null,
    val eventsUrl: String? = null,
    val aboutUrl: String? = null,
    val menuUrl: String? = null,
    val imageRef: Url? = null,
) {
    val isValid get() = name != null && geoPoint != null

    val invalidPart get() = when {
        name.isNullOrBlank() -> "name"
        geoPoint == null -> "geolocation"
        city == null -> "city"
        else -> null
    }

    val displayTitle get() = name ?: address ?: "(geocoordinates)"

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
}

fun LocationEdit.mergeLeft(edit: LocationEdit?) = edit?.let {
    LocationEdit(
        locationId = locationId ?: edit.locationId,
        name = name ?: edit.name,
        city = city ?: edit.city,
        description = description ?: edit.description,
        address = address ?: edit.address,
        notes = notes ?: edit.notes,
        geoPoint = geoPoint ?: edit.geoPoint,
        mapId = mapId ?: edit.mapId,
        mapRank = mapRank ?: edit.mapRank,
        mapCategory = mapCategory ?: edit.mapCategory,
        mapType = mapType ?: edit.mapType,
        resources = resources ?: edit.resources,
        website = website ?: edit.website,
        eventsUrl = eventsUrl ?: edit.eventsUrl,
        aboutUrl = aboutUrl ?: edit.aboutUrl,
        menuUrl = menuUrl ?: edit.menuUrl,
        imageRef = imageRef ?: edit.imageRef?.takeIf { it.value.isNotEmpty() },
    )
} ?: this

fun LocationEdit.mergeRight(edit: LocationEdit?) = edit?.mergeLeft(this) ?: this

fun OSMLocation.toEdit() = LocationEdit(
    mapId = osmId,
    name = name,
    address = address.road?.let { road ->
        address.number?.let { number ->
            "$number $road"
        } ?: road
    } ?: error("address not found"),
    city = address.city,
    state = address.state ?: error("state not found"),
    country = address.country ?: error("country not found"),
    geoPoint = toGeoPoint(),
    mapRank = importance?.toFloat() ?: 0f,
    mapCategory = category,
    mapType = type,
    website = extraTags?.website
)

fun OSMLocation.toEditOrNull() = runCatching { toEdit() }.getOrNull()