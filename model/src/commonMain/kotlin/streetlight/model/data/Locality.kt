package streetlight.model.data

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable

@Serializable
data class Locality(
    val cityId: CityId,
    val city: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
    val geoRank: Float,
    val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
)