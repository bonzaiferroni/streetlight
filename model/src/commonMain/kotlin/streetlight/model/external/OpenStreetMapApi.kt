package streetlight.model.external

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import streetlight.model.data.PlaceProto

@Serializable
data class OSMLocation(
    @SerialName("place_id")
    val placeId: Long,
    val licence: String,
    @SerialName("osm_type")
    val osmType: String,
    @SerialName("osm_id")
    val osmId: Long,
    val lat: Double,
    val lon: Double,
    @SerialName("class")
    val nodeClass: String,
    val type: String,
    @SerialName("place_rank")
    val placeRank: Int,
    val importance: Double? = null,
    @SerialName("addresstype")
    val addressType: String, // non-nullable if addressdetails=1
    @SerialName("house_number")
    val houseNumber: Int? = null,
    val name: String? = null,
    @SerialName("display_name")
    val displayName: String,
    val address: Address,
    @SerialName("boundingbox")
    val bounds: List<Double>, // [south, north, west, east]
    @SerialName("extratags")
    val extraTags: OSMExtra? = null
)

@Serializable
data class Address(
    @SerialName("house_number")
    val number: String? = null,
    val road: String? = null,
    val city: String? = null,
    val state: String? = null,
    @SerialName("ISO3166-2-lvl4")
    val iso3166Lvl4: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    @SerialName("country_code")
    val countryCode: String? = null
)

@Serializable
data class OSMExtra(
    val website: String? = null,
    @SerialName("contact:website")
    val contactWebsite: String? = null,
    @SerialName("contact:email")
    val contactEmail: String? = null,
    @SerialName("contact:phone")
    val contactPhone: String? = null,
    val phone: String? = null,
    val email: String? = null,

    val opening_hours: String? = null,
    val cuisine: String? = null,
    val takeaway: String? = null,
    val delivery: String? = null,

    val wikidata: String? = null,
    val wikipedia: String? = null,
    val brand: String? = null,
    val operator: String? = null,

    val capacity: String? = null,
    val height: String? = null,
    val levels: String? = null,
)

@Serializable
data class OSMQuery(
    val query: String? = null,
    val amenity: String? = null,
    val street: String? = null,
    val city: String? = null,
    val county: String? = null,
    val state: String? = null,
    val country: String? = null,
    val postalcode: String? = null,
    val bounds: GeoBounds? = null,
    val limit: Int = 10
)

fun OSMLocation.toGeoPoint() = GeoPoint(
    lat = lat,
    lng = lon
)

fun OSMLocation.toGeoBounds() = GeoBounds(
    sw = GeoPoint(lat = bounds[0], lng = bounds[2]),
    ne = GeoPoint(lat = bounds[1], lng = bounds[3])
)

@Serializable
data class OSMCity(
    val name: String,
    val state: String,
    val country: String,
    val importance: Float,
    val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
)

fun OSMLocation.toOSMCity() = OSMCity(
    name = name ?: error("name not found"),
    state = address.state ?: "",
    country = address.country ?: "",
    importance = importance?.toFloat() ?: 0f,
    geoPoint = toGeoPoint(),
    geoBounds = toGeoBounds(),
)

fun OSMLocation.toOSMCityOrNull() = runCatching {
    toOSMCity()
}.getOrNull()

fun OSMLocation.toPlaceProto() = PlaceProto(
    name = name,
    address = address.road?.let { road ->
        address.number?.let { number ->
            "$number $road"
        } ?: road
    },
    city = address.city,
    geoPoint = toGeoPoint(),
    website = extraTags?.website
)

//{
//    "place_id": 306281058,
//    "licence": "Data © OpenStreetMap contributors, ODbL 1.0. http://osm.org/copyright",
//    "osm_type": "node",
//    "osm_id": 5029710203,
//    "lat": 39.7157006,
//    "lon": -104.8216956,
//    "class": "shop",
//    "type": "mobile_phone",
//    "place_rank": 30,
//    "importance": 0.00006197833218886118,
//    "addresstype": "shop",
//    "name": "Target Mobile",
//    "display_name": "Target Mobile, 14200, East Ellsworth Avenue, Aurora City Place, Aurora, Arapahoe County, Colorado, 80012, United States",
//    "address": {
//        "house_number": "14200",
//        "road": "East Ellsworth Avenue",
//        "city": "Aurora",
//        "state": "Colorado",
//        "ISO3166-2-lvl4": "US-CO",
//        "postcode": "80012",
//        "country": "United States",
//        "country_code": "us"
//    },
//    "boundingbox": [
//        39.7156506,
//        39.7157506,
//        -104.8217456,
//        -104.8216456
//    ]
//}