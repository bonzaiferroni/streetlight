package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kampfire.model.toValidityCheck
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
    val validity by lazy {
        buildSet {
            if (name.isNullOrBlank()) add(LocationProperty.Name)
            if (geoPoint == null) add(LocationProperty.GeoPoint)
            if (city == null) add(LocationProperty.City)
        }.toValidityCheck()
    }

    val displayTitle get() = name ?: address ?: "(geolocation)"
}

object LocationProperty {
    val Name = "name"
    val GeoPoint = "geolocation"
    val City = "city"
}

fun LocationEdit.mergeLeft(edit: LocationEdit?) = edit?.let {
    LocationEdit(
        locationId = locationId ?: edit.locationId,
        name = name ?: edit.name,
        city = city ?: edit.city,
        state = state ?: edit.state,
        country = country ?: edit.country,
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
    name = name?.takeIf { it.isNotBlank() },
    address = address.road?.let { road ->
        address.number?.let { number ->
            "$number $road"
        } ?: road
    },
    city = address.city,
    state = address.state,
    country = address.country,
    geoPoint = toGeoPoint(),
    mapRank = importance?.toFloat() ?: 0f,
    mapCategory = category,
    mapType = type,
    website = extraTags?.website
)

fun OSMLocation.toEditOrNull() = runCatching { toEdit() }.getOrNull()