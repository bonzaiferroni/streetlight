package streetlight.model.data

import kampfire.api.Slug
import kampfire.api.Username
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface GalaxyPost {
    val postId: PostId
    val slug: Slug
    val galaxyId: GalaxyId
    val galaxyName: String
    val galaxySlug: Slug
    val username: Username?
    val text: String?
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val label: String
    val sublabel: String?
    val description: String?
    val lightCount: Int
    val isLit: Boolean
    val links: List<ExtraLink>?
    val createdAt: Instant
    val updatedAt: Instant

    val postType: PostType
}

@Serializable
@JvmInline
value class PostId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = PostId(Uuid.random())
    }
}

enum class PostOrder(label: String? = null) {
    NewFirst("Newest first"),
    OldFirst("Oldest first");
    // Visibility;

    val label = label ?: name
}