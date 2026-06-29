package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toMarkdown
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.toUrl
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Location(
    val locationId: LocationId,
    val cityId: CityId?,
    val mapId: MapId?,
    val timezoneId: String,
    val slug: Slug,
    val scout: Username?,
    val host: Username?,
    val name: String?,
    val description: Markdown?,
    val address: String?,
    val city: String?,
    val state: String?,
    val geoPoint: GeoPoint,
    val mapRank: Float?,
    val mapCategory: String?,
    val mapType: String?,
    val resources: Set<ResourceType>,
    val hours: HoursSchedule?,
    val website: String?,
    val lightCount: Int?,
    val eventsUrl: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val extraLinks: List<ExtraLink>?,
    val updatedAt: Instant,
    val createdAt: Instant,
): Labeled {
    val isLit get() = false

    val addressLine by lazy {
        addressLineOf(address, city)
    }

    override val label get() = name ?: address ?: "(geolocation)"
    val sublabel get() = when (name) {
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
            extraLinks?.let {
                addAll(it)
            }
        }.takeIf { it.isNotEmpty() }
    }

    // td: implement contact info
    val phone: String? get() = null
    val email: String? get() = null
}

@JvmInline @Serializable
value class LocationId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object { fun random() = LocationId(Uuid.random())}
}

@Serializable
data class LocationAddress(
    val streetAddress: String,
    val postCode: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
)

fun Location.toEdit() = LocationEdit(
    locationId = locationId,
    mapId = mapId,
    timezoneId = timezoneId,
    name = name,
    description = description,
    address = address,
    city = city,
    state = state,
    geoPoint = geoPoint,
    mapRank = mapRank, // td: track rank some other way
    mapCategory = mapCategory,
    mapType = mapType,
    resources = resources,
    hours = hours,
    website = website,
    eventsUrl = eventsUrl,
    extraLinks = extraLinks,
    imageRef = imageRef,
)

fun PlaceProto.toEdit() = LocationEdit(
    name = name,
    address = address,
    city = city,
    geoPoint = geoPoint,
    website = website
)

fun LocationParse.toEdit(
    locationId: LocationId? = null
) = LocationEdit(
    locationId = locationId,
    name = name ?: "",
    description = description?.toMarkdown(),
    address = address,
//    val postalCode: String? = null,
    city = city,
//    val state: String? = null,
//    val country: String? = null,
    website = url,
    eventsUrl = eventsUrl,
    extraLinks = buildList {
        menuUrl?.let { add(ExtraLink("menu", it)) }
        aboutUrl?.let { add(ExtraLink("about", it))}
    },
    imageRef = imageUrl?.toUrl(),
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

