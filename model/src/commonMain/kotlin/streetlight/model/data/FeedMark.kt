package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class FeedMark(
    val markId: MarkId,
    val lean: Lean,
    val name: String
) {
    companion object {
        val ValidLength = IntRange(2, 32)
    }
}

@JvmInline @Serializable
value class MarkId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

enum class Lean(override val label: String, val value: Int): Labeled {
    StrongNegative("-2", -2),
    Negative("-1", -1),
    Neutral("0", 0),
    Positive("+1", +1),
    StrongPositive("+2", +2),
}

@Serializable
data class PostMark(
    val markId: MarkId,
    val sum: Int,
    val isMarked: Boolean,
)

fun findLight(feedMarks: List<FeedMark>, postMarks: List<PostMark>?) = postMarks?.sumOf { postMark ->
    feedMarks.firstOrNull { it.markId == postMark.markId }?.lean?.value ?: 0
} ?: 0

enum class VoteType { Single, Polar, Multi }

fun List<FeedMark>.getVoteType(): VoteType? {
    if (isEmpty()) return null
    if (size == 1) return VoteType.Single
    if (size == 2 && any { it.lean.value > 0} && any { it.lean.value < 0 }) return VoteType.Polar
    return VoteType.Multi
}