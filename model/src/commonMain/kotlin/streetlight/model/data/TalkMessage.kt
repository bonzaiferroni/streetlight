package streetlight.model.data

import kotlinx.serialization.Serializable

@Serializable
sealed interface TalkMessage

@Serializable
data class TalkHistory(
    val comments: List<Comment>
): TalkMessage

@Serializable
data class TalkComment(
    val comment: Comment
): TalkMessage

@Serializable
sealed interface TalkRequest

