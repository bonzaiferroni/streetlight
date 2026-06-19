package streetlight.model.data

import kampfire.api.Markdown
import kampfire.api.Username
import kampfire.model.Url
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Comment(
    val commentId: CommentId,
    val parentId: CommentId?,
    val username: Username?,
    val thumb: Url?,
    val text: Markdown,
    val lightCount: Int,
    val replyCount: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class CommentId(override val value: Uuid) : RecordId {
    companion object {
        fun random() = CommentId(Uuid.random())
    }

    override fun toString() = value.toString()
}

@Serializable
data class NewComment(
    val spaceId: Uuid,
    val spaceType: SpaceType,
    val parentId: CommentId?,
    val text: Markdown,
) {
    val galaxyId get() = if (spaceType == SpaceType.Galaxy) GalaxyId(spaceId) else error("invalid SpaceType")
    val postId get() = if (spaceType == SpaceType.Post) PostId(spaceId) else error("invalid SpaceType")
}

@Serializable
data class UpdatedComment(
    val commentId: CommentId,
    val spaceId: Uuid,
    val text: Markdown,
)