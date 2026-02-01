package streetlight.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimPlace(
    @SerialName("place_id")
    val placeId: Long,
    val licence: String,
    @SerialName("osm_type")
    val osmType: String,
    @SerialName("osm_id")
    val osmId: Long,
    val lat: String,
    val lon: String,
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
    val bounds: List<String>
)

@Serializable
data class Address(
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
