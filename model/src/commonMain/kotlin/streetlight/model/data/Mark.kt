package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

@Serializable
data class Mark(
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
data class MarkStatus(
    val markId: MarkId,
    val sum: Int,
    val isMarked: Boolean,
)

enum class VoteType { Single, Polar, Multi }

fun List<Mark>.getVoteType(): VoteType? {
    if (isEmpty()) return null
    if (size == 1) return VoteType.Single
    if (size == 2 && any { it.lean.value > 0} && any { it.lean.value < 0 }) return VoteType.Polar
    return VoteType.Multi
}

fun curatorStatusOf(postId: PostId, marks: List<Mark>, postMarks: List<MarkStatus>?) = marks.getVoteType()?.let {
    CuratorStatus(postId, it, marks, postMarks)
}

@Serializable
data class CuratorStatus(
    val postId: PostId,
    val voteType: VoteType,
    val marks: List<Mark>,
    val postMarks: List<MarkStatus>?,
) {
    val postTally get() = postMarks?.sumOf { postMark ->
        marks.firstOrNull { it.markId == postMark.markId }?.lean?.value ?: 0
    } ?: 0
}

@Serializable
data class MarkUpdate(
    val markId: MarkId,
    val postId: PostId,
    val isMarked: Boolean,
)