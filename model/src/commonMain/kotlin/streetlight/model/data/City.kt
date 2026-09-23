package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoRect
import kampfire.model.GeoPoint
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class City(
    val cityId: CityId,
    val slug: Slug,
    val name: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
    val locationCount: Int,
    val mapRank: Float?,
    override val geoPoint: GeoPoint,
    val geoRect: GeoRect,
): Entity {
    override val label get() = name
    override val markerId get() = slug.value
}

@JvmInline @Serializable
value class CityId(override val value: Uuid): RecordId {
    companion object { fun random() = CityId(Uuid.random())}
    override fun toString() = value.toString()
}