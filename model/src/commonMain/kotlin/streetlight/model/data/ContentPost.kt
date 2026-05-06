package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.ScaledImageArray
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ContentPost(
    override val postId: PostId,
    override val galaxyId: GalaxyId,
    override val username: String?,
    override val title: String,
    override val text: String?,
    override val geoPoint: GeoPoint?,
    val imageRef: Url?,
    override val images: ScaledImageArray?,
    override val links: List<ExtraLink>?,
    override val createdAt: Instant,
    override val updatedAt: Instant,
): Post {

    override val visibility: Int get() = 0
    override val description get() = text
    override val postType get() = PostType.Content
}

@Serializable
data class ContentEdit(
    val postId: PostId?,
    val galaxyId: GalaxyId,
    val title: String? = null,
    val text: String? = null,
    val geoPoint: GeoPoint? = null,
    val imageRef: Url? = null,
    val links: List<ExtraLink>? = null,
) {
    val invalidPart get() = when {
        title.isNullOrBlank() -> "title"
        text.isNullOrBlank() -> "content"
        else -> null
    }

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
    val isValid get() = invalidPart == null
}

fun ContentPost.toEdit() = ContentEdit(
    postId = postId,
    galaxyId = galaxyId,
    title = title,
    text = text,
    geoPoint = geoPoint,
    imageRef = imageRef,
    links = links
)