package streetlight.model.data

import kampfire.api.Markdown
import kotlinx.serialization.Serializable

/** A message on the talk socket, which keeps a space's comments live. */
@Serializable
sealed interface TalkMessage

/** A comment was added. */
@Serializable
data class CommentCreated(
    val comment: Comment
): TalkMessage

/** A comment's text was changed. */
@Serializable
data class CommentUpdated(
    val commentId: CommentId,
    val text: Markdown,
): TalkMessage

/** A request sent on the talk socket. */
@Serializable
sealed interface TalkRequest

