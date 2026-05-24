package streetlight.model.data

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
    val description: String?,
    val city: String?,
    val geoPoint: GeoPoint,
    val geoBounds: GeoBounds,
    val postPermission: PostPermission,
    val reviewMode: ReviewMode,
    val postGuide: String?,
    val imageRef: Url?,
    val images: ScaledImageArray?,
    val lightCount: Int?,
    val eventCount: Int?,
    val locationCount: Int?,
    val updatedAt: Instant,
    val createdAt: Instant,
) {
    val postCount get() = eventCount?.let {
        it + (locationCount ?: 0)
    }

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

enum class ReviewMode(val label: String) {
    PostImmediately("User posts appear immediately"),
    PostAfterReview("User posts appear after reviewed"),
}