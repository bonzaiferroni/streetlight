package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.model.GeoRect
import kampfire.model.GeoPoint
import koala.Image
import koala.model.RouteContent
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A city, with the counts of its locations, upcoming events and galaxies. */
@Serializable
data class City(
    val cityId: CityId,
    val slug: Slug,
    val name: String,
    val state: String,
    val country: String,
    val galaxyCount: Int,
    val locationCount: Int,
    val eventCount: Int,
    override val image: Image?,
    val description: Markdown?,
    override val links: List<ExtraLink>?,
    val mapRank: Float?,
    override val geoPoint: GeoPoint,
    val geoRect: GeoRect,
): Entity, RouteContent {
    override val label get() = name
    override val body get() = description
    override val markerId get() = slug.value
}

@JvmInline @Serializable
value class CityId(override val value: Uuid): RecordId {
    companion object { fun random() = CityId(Uuid.random())}
    override fun toString() = value.toString()
}