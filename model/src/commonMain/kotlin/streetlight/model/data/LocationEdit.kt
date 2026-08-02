package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.Url
import kampfire.model.toUrl
import kampfire.model.toValidityCheck
import kampfire.utils.snakeToTitleCase
import koala.Image
import kotlinx.serialization.Serializable
import streetlight.model.external.OSMLocation
import streetlight.model.external.toGeoPoint
import streetlight.model.external.toHoursSchedule

@Serializable
data class LocationEdit(
    val locationId: LocationId? = null,
    val cityId: CityId? = null,
    val timezoneId: String? = null,
    val name: String? = null,
    val city: String? = null,
    val description: Markdown? = null,
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
    val hours: HoursSchedule? = null,
    val website: Url? = null,
    val eventsUrl: Url? = null,
    val extraLinks: List<ExtraLink>? = null,
    override val image: Image? = null,
): Labeled, RecordEdit {
    override val recordType get() = RecordType.Location

    val validity by lazy {
        buildSet {
            if (name.isNullOrBlank()) add(LocationProperty.Name)
            if (geoPoint == null) add(LocationProperty.GeoPoint)
            if (city == null) add(LocationProperty.City)
        }.toValidityCheck()
    }

    val addressLine by lazy {
        addressLineOf(address, city)
    }

    override val label get() = name ?: address ?: "(geolocation)"
    val subLabel get() = when (name) {
        null -> city
        else -> addressLine
    }

    val links by lazy {
        buildList {
            website?.let {
                add(ExtraLink("website", it))
            }
            eventsUrl?.let {
                add(ExtraLink("calendar", it))
            }
//            extraLinks?.let {
//                addAll(it)
//            }
        }.takeIf { it.isNotEmpty() }
    }

    val needsReview get() = image == null || website == null || description == null || description.length < 100
}

object LocationProperty {
    val Name = "name"
    val GeoPoint = "geolocation"
    val City = "city"
}

fun LocationEdit.mergeLeft(edit: LocationEdit?) = edit?.let {
    LocationEdit(
        locationId = locationId ?: edit.locationId,
        timezoneId = timezoneId ?: edit.timezoneId,
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
        hours = hours ?: edit.hours,
        website = website ?: edit.website,
        eventsUrl = eventsUrl ?: edit.eventsUrl,
        extraLinks = extraLinks ?: edit.extraLinks,
        image = image ?: edit.image?.takeIf { it.value.isNotEmpty() },
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
    hours = toHoursSchedule(),
    city = address.city,
    state = address.state,
    country = address.country,
    geoPoint = toGeoPoint(),
    mapRank = importance?.toFloat() ?: 0f,
    mapCategory = category,
    mapType = type.snakeToTitleCase(),
    website = extraTags?.website?.toUrl()
)

fun OSMLocation.toEditOrNull() = runCatching { toEdit() }.getOrNull()