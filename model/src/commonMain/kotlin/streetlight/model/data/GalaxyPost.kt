package streetlight.model.data

import kampfire.api.Slug
import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface GalaxyPost {
    val postId: PostId
    val slug: Slug
    val galaxyId: GalaxyId
    val username: String?
    val text: String?
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val title: String
    val description: String?
    val boosts: Int
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