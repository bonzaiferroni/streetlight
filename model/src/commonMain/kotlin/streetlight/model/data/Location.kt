package streetlight.model.data

import kampfire.model.GeoPoint
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
    val mapId: MapId?,
    val name: String?,
    val username: String?,
    val description: String?,
    val address: String?,
    val city: String?,
    val geoPoint: GeoPoint,
    val mapRank: Float?,
    val mapClass: String?,
    val mapType: String?,
    val resources: Set<ResourceType>,
    val website: String?,
    val lightCount: Int?,
    val eventsUrl: String?,
    val menuUrl: String?,
    val aboutUrl: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val extraLinks: List<ExtraLink>?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val addressLine by lazy {
        addressLineOf(address, city)
    }

    val displayTitle get() = name ?: address ?: "(geocoordinates)"

    val links by lazy {
        buildList {
            website?.let {
                add(ExtraLink("website", it))
            }
            eventsUrl?.let {
                add(ExtraLink("calendar", it))
            }
            menuUrl?.let {
                add(ExtraLink("menu", it))
            }
            extraLinks?.let {
                addAll(it)
            }
        }.takeIf { it.isNotEmpty() }
    }

    val phone: String? get() = null
    val email: String? get() = null
}

@JvmInline @Serializable
value class LocationId(override val value: Uuid): ProjectId {
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
    name = name,
    description = description,
    address = address,
    city = city,
    geoPoint = geoPoint,
    resources = resources,
    website = website,
    eventsUrl = eventsUrl,
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
    description = description,
    address = address,
//    val postalCode: String? = null,
    city = city,
//    val state: String? = null,
//    val country: String? = null,
    website = url,
    eventsUrl = eventsUrl,
    menuUrl = menuUrl,
    aboutUrl = aboutUrl,
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

