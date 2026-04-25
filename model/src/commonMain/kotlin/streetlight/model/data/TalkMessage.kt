package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface TalkMessage

@Serializable
data class TalkHistory(
    val comments: List<Comment>
): TalkMessage

@Serializable
data class CommentCreated(
    val comment: Comment
): TalkMessage

@Serializable
data class CommentUpdated(
    val commentId: CommentId,
    val text: String,
): TalkMessage

@Serializable
sealed interface TalkRequest

