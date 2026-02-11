package streetlight.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OSMPlace(
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
    val placeClass: String,
    val type: String,
    @SerialName("place_rank")
    val placeRank: Int,
    val importance: Double,
    @SerialName("addresstype")
    val addressType: String,
    @SerialName("house_number")
    val houseNumber: Int? = null,
    val name: String,
    @SerialName("display_name")
    val displayName: String,
    val address: Address,
    @SerialName("boundingbox")
    val bounds: List<Double>
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