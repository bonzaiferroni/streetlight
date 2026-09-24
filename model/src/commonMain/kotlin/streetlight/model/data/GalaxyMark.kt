package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.uuid.Uuid

/** A mark a galaxy's members can put on a post, leaning it up or down in the feed. */
@Serializable
data class GalaxyMark(
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
    companion object {
        val Empty get() = MarkId(Uuid.NIL)
    }

    override fun toString() = value.toString()
}

/** How far a mark leans a post in its galaxy's feed. */
enum class Lean(override val label: String, val value: Int): Labeled {
    StrongPositive("+2", +2),
    Positive("+1", +1),
    Neutral("0", 0),
    Negative("-1", -1),
    StrongNegative("-2", -2),
}

/** The count of a mark on a post, and whether the caller placed it. */
@Serializable
data class MarkTally(
    val markId: MarkId,
    val count: Int,
    val isMarked: Boolean,
)

/** How a galaxy's marks are placed: a single mark, a positive and negative pair, or several. */
enum class CuratorType { Single, Polar, Multi }

/** The [CuratorType] of these marks, or `null` when there are none. */
fun List<GalaxyMark>.getCuratorType(): CuratorType? {
    if (isEmpty()) return null
    if (size == 1) return CuratorType.Single
    if (size == 2 && any { it.lean.value > 0 } && any { it.lean.value < 0 } ) return CuratorType.Polar
    return CuratorType.Multi
}

/** The [CuratorStatus] of the post [postId], from its galaxy's [marks] and its tallies. */
fun curatorStatusOf(postId: PostId, marks: List<GalaxyMark>, postMarks: List<MarkTally>?) = marks.getCuratorType()?.let { voteType ->
    CuratorStatus(postId, voteType, marks.map { mark ->
        val postMark = postMarks?.firstOrNull { it.markId == mark.markId }
        FeedMark(
            markId = mark.markId,
            lean = mark.lean,
            name = mark.name,
            count = postMark?.count ?: 0,
            isMarked = postMark?.isMarked ?: false
        )
    })
}

/** The marks a post carries in its galaxy, shown by its curator badge. */
@Serializable
data class CuratorStatus(
    val postId: PostId,
    val curatorType: CuratorType,
    val marks: List<FeedMark>,
) {
    /** The sum of each mark's lean times its count. */
    val postLean get() = marks.sumOf { it.lean.value * it.count }
    val maxCount get() = marks.maxOf { it.count }

    /** The count of [markId] as a fraction of the highest count, or `null` when no mark is placed. */
    fun progressOf(markId: MarkId) = maxCount.takeIf { it > 0 }?.let { max ->
        marks.first { it.markId == markId }.count / max.toFloat()
    } ?: 0f
}

/** A request to place or remove a mark on a post. */
@Serializable
data class MarkUpdate(
    val markId: MarkId,
    val postId: PostId,
    val isMarked: Boolean,
)

/** A mark of a galaxy, with its count on a post and whether the caller placed it. */
@Serializable
data class FeedMark(
    val markId: MarkId,
    val lean: Lean,
    val name: String,
    val count: Int,
    val isMarked: Boolean,
)