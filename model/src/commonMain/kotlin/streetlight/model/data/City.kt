package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class City(
    val cityId: CityId,
    val slug: Slug,
    val name: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
    val mapRank: Float?,
    override val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
): StreetPost {
    override val label get() = name
}

@JvmInline @Serializable
value class CityId(val value: Int) {
    override fun toString() = value.toString()

    companion object {
        val empty = CityId(0)
    }
}