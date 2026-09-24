package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Slug
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

/** The fields of a post as it is sent: the galaxy, the kind of record shared, and its title and text. */
@Serializable
data class PostEdit(
    val postId: PostId?,
    val galaxyId: GalaxyId,
    val postType: PostType,
    val recordId: Uuid,
    val title: String? = null,
    val text: Markdown? = null,
) {
    /** The first part that is missing, or `null`. */
    val invalidPart get() = when {
        title.isNullOrBlank() -> "title"
        text?.value.isNullOrBlank() -> "content"
        else -> null
    }

    val invalidMessage get() = invalidPart?.let { "missing: $it"}
    val isValid get() = invalidPart == null
}

/** An edit of this post, sharing the record [recordId] of [postType]. */
fun Post.toEdit(recordId: Uuid, postType: PostType) = PostEdit(
    postId = postId,
    galaxyId = galaxy.galaxyId,
    recordId = recordId,
    postType = postType,
    title = title,
    text = text,
)