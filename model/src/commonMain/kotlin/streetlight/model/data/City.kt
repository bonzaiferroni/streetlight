package streetlight.model.data

import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class City(
    val cityId: CityId,
    val name: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
    val mapRank: Float?,
    val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
)

@JvmInline @Serializable
value class CityId(val value: Int) {
    companion object {
        val empty = CityId(0)
    }
}