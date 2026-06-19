package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kampfire.model.GeoBounds
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Galaxy(
    val galaxyId: GalaxyId,
    val cityId: CityId?,
    val slug: Slug,
    val name: String,
    val tagline: String?,
    val description: Markdown?,
    val city: String?,
    val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
    val postPermission: PostPermission,
    val reviewCount: Int,
    val postGuide: Markdown?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val isLit: Boolean,
    val isHost: Boolean,
    val starCount: Int,
    val eventCount: Int,
    val locationCount: Int,
    val postCount: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val postTypes get() = setOf(PostType.Location, PostType.Event, PostType.Content)
}

@JvmInline @Serializable
value class GalaxyId(override val value: Uuid): RecordId {
    companion object { fun random() = GalaxyId(Uuid.random())}
    override fun toString() = value.toString()
}

enum class PostPermission(label: String? = null) {
    Everyone,
    Accounts("Streetlight accounts"),
    Founder;

    val label = label ?: name
}