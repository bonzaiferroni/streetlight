package streetlight.model.data

import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable

@Serializable
data class PostEdit(
    val postId: PostId?,
    val galaxyId: GalaxyId,
    val title: String? = null,
    val text: String? = null,
    val subtitle: String? = null,
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

fun BasicPost.toEdit() = PostEdit(
    postId = postId,
    galaxyId = galaxyId,
    title = title,
    subtitle = subtitle,
    text = text,
    geoPoint = geoPoint,
    imageRef = imageRef,
    links = links
)