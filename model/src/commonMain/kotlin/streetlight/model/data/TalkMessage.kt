package streetlight.model.data

import kampfire.api.Markdown
import kotlinx.serialization.Serializable

@Serializable
sealed interface TalkMessage

@Serializable
data class CommentCreated(
    val comment: Comment
): TalkMessage

@Serializable
data class CommentUpdated(
    val commentId: CommentId,
    val text: Markdown,
): TalkMessage

@Serializable
sealed interface TalkRequest

