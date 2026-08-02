package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.api.Username
import kampfire.api.toMarkdown
import kampfire.model.GeoPoint
import kampfire.model.Labeled
import kampfire.model.Url
import kampfire.model.toUrl
import koala.Image
import koala.model.RouteContent
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
    override val geoPoint: GeoPoint,
    val mapRank: Float?,
    val mapCategory: String?,
    val mapType: String?,
    val resources: Set<ResourceType>,
    val hours: HoursSchedule?,
    val website: Url?,
    val lightCount: Int?,
    val isLit: Boolean,
    val eventsUrl: Url?,
    override val image: Image?,
    val extraLinks: List<ExtraLink>?,
    val updatedAt: Instant,
    val createdAt: Instant,
): Entity, Labeled, RouteContent {

    val addressLine by lazy {
        addressLineOf(address, city)
    }

    override val label get() = name ?: address ?: "(geolocation)"
    override val sublabel get() = when (address) {
        null -> city
        else -> address
    }
    override val body get() = description

    override val links by lazy {
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
    image = image,
)

fun PlaceProto.toEdit() = LocationEdit(
    name = name,
    address = address,
    city = city,
    geoPoint = geoPoint,
    website = website?.toUrl()
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
    website = url?.toUrl(),
    eventsUrl = eventsUrl?.toUrl(),
    extraLinks = buildList {
        menuUrl?.let { add(ExtraLink("menu", it.toUrl())) }
        aboutUrl?.let { add(ExtraLink("about", it.toUrl()))}
    },
    image = imageUrl?.toUrl()?.let { Image(it) },
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

