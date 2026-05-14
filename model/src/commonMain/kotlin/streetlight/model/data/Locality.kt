package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
data class Locality(
    val cityId: CityId?,
    val city: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
)