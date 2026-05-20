package streetlight.model.data

import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable
import streetlight.model.external.OSMLocation
import streetlight.model.external.toGeoPoint

@Serializable
data class MapLocation(
    val mapId: MapId,
    val name: String?,
    val address: String,
    val postCode: String?,
    val city: String?,
    val state: String,
    val country: String,
    val geoPoint: GeoPoint,
    val mapRank: Float,
    val mapClass: String,
    val mapType: String,
    val website: String?,
)

typealias MapId = Long

@Serializable
data class PlaceProto(
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

fun OSMLocation.toMapLocation() = MapLocation(
    mapId = osmId,
    name = name,
    address = address.road?.let { road ->
        address.number?.let { number ->
            "$number $road"
        } ?: road
    } ?: error("address not found"),
    postCode = address.postcode,
    city = address.city,
    state = address.state ?: error("state not found"),
    country = address.country ?: error("country not found"),
    geoPoint = toGeoPoint(),
    mapRank = importance?.toFloat() ?: 0f,
    mapClass = category,
    mapType = type,
    website = extraTags?.website
)

fun OSMLocation.toMapLocationOrNull() = runCatching { toMapLocation() }.getOrNull()