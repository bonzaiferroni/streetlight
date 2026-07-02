package streetlight.model.data

import kampfire.api.Markdown
import kampfire.model.GeoPoint
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class PostEdit(
    val postId: PostId?,
    val galaxyId: GalaxyId,
    val postType: PostType,
    val recordId: Uuid,
    val title: String? = null,
    val text: Markdown? = null,
) {
    val invalidPart get() = when {
        title.isNullOrBlank() -> "title"
        text?.value.isNullOrBlank() -> "content"
        else -> null
    }

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
    val isValid get() = invalidPart == null
}

fun Post.toEdit(recordId: Uuid, postType: PostType) = PostEdit(
    postId = postId,
    galaxyId = galaxyId,
    recordId = recordId,
    postType = postType,
    title = title,
    text = text,
)