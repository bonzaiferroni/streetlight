package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Review(
    val reviewId: ReviewId,
    val quorumId: QuorumId,
    val starId: StarId,
    val decision: Int?,
    val taskStatus: TaskStatus,
    val updatedAt: Instant,
    val createdAt: Instant,
) {

}

@Serializable
@JvmInline
value class ReviewId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

enum class ContentStatus {
    Submitted,
    Accepted,
    Rejected,
}

enum class TaskStatus {
    Requested,
    Accepted,
    Completed,
    Dismissed,
}

enum class FeedStatus {
    Reviewing,
    Live,
    Moderating,
    Removed,
}