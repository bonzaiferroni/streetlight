package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.model.toUrl
import kampfire.utils.randomUuidString
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class Location(
    val locationId: LocationId,
    val name: String,
    val username: String?,
    val description: String?,
    val address: String?,
    val city: String,
    // td: addressNumber
    // td: street
    // td: unit/suite
    // td: postalCode
    // td: state
    // td: country
    val geoPoint: GeoPoint,
    val resources: Set<ResourceType>,
    val website: String?,
    val eventsUrl: String?,
    val menuUrl: String?,
    val aboutUrl: String?,
    // td: create -> tags
    // td: aboutLink
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val extraLinks: List<ExtraLink>?,
    val updatedAt: Instant,
    val createdAt: Instant,
    // td: openedAt: LocalDate
    // td: LocationTags
) {
    val addressLine by lazy {
        addressLineOf(address, city)
    }

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
}

@JvmInline @Serializable
value class LocationId(override val value: String): ProjectId {
    companion object { fun random() = LocationId(randomUuidString())}
}

@Serializable
data class LocationEdit(
    val locationId: LocationId? = null,
    val name: String? = null,
    val city: String? = null,
    val isOwner: Boolean = false,
    val description: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val geoPoint: GeoPoint? = null,
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

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
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
    website = website,
    eventsUrl = eventsUrl,
    imageRef = imageRef,
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

//fun Place.toLocation() = Location(
//    locationId = LocationId.random(),
//    name = name ?: "",
//    geoPoint = geoPoint ?: GeoPoint.Denver,
//    description = null,
//    address = address,
//    resources = emptySet(),
//    website = null,
//    eventsUrl = null,
//    aboutUrl = null,
//    menuUrl = null,
//    imageUrl = null,
//    imageMd = null,
//    imageSm = null,
//    updatedAt = Clock.System.now(),
//    createdAt = Clock.System.now()
//)

fun Place.toEdit() = LocationEdit(
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
    city = null,
//    val state: String? = null,
//    val country: String? = null,
    website = url,
    eventsUrl = eventsUrl,
    menuUrl = menuUrl,
    aboutUrl = aboutUrl,
    imageRef = imageUrl?.toUrl(),
)

//     val name: String? = null,
//    val description: String? = null,
//    val address: String? = null,
//    val postalCode: String? = null,
//    val city: String? = null,
//    val state: String? = null,
//    val country: String? = null,
//    val url: String? = null,
//    val eventsUrl: String? = null,
//    val aboutUrl: String? = null,
//    val menuUrl: String? = null,
//    val imageUrl: String? = null,

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
        eventsUrl = eventsUrl ?: edit.eventsUrl,
        aboutUrl = aboutUrl ?: edit.aboutUrl,
        menuUrl = menuUrl ?: edit.menuUrl,
        imageRef = imageRef ?: edit.imageRef,
    )
} ?: this

fun LocationEdit.mergeRight(edit: LocationEdit?) = edit?.mergeLeft(this) ?: this