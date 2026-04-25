package streetlight.model.data

import kampfire.api.StringId
import kampfire.model.Url
import kampfire.utils.randomUuidString
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant

@Serializable
data class Comment(
    val commentId: CommentId,
    val parentId: CommentId?,
    val username: String?,
    val thumb: Url?,
    val text: String,
    val lightCount: Int,
    val replyCount: Int,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class CommentId(override val value: String) : ProjectId {
    companion object {
        fun random() = CommentId(randomUuidString())
    }

    override fun toString() = value
}

@Serializable
data class NewComment(
    val spaceId: StringId,
    val spaceType: SpaceType,
    val parentId: CommentId?,
    val text: String,
) {
    val galaxyId get() = if (spaceType == SpaceType.Galaxy) GalaxyId(spaceId) else error("invalid SpaceType")
}
