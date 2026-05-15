package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
sealed interface Post {
    val postId: PostId
    val galaxyId: GalaxyId
    val username: String?
    val userThumb: Url?
    val text: String?
    val images: ScaledImageArray?
    val geoPoint: GeoPoint?
    val title: String
    val description: String?
    val visibility: Int
    val links: List<ExtraLink>?
    val createdAt: Instant
    val updatedAt: Instant

    val postType: PostType
}

@Serializable
@JvmInline
value class PostId(override val value: Uuid): ProjectId {
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