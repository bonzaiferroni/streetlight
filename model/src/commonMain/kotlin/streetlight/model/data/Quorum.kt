package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * A question about a record put to users, such as whether a location is real, and its decision once enough have
 * answered.
 */
@Serializable
data class Quorum(
    val quorumId: QuorumId,
    val recordId: Uuid,
    val recordType: RecordType,
    val question: Question,
    val decision: Int?,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class QuorumId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

/** The questions a [Quorum] asks, each with the number of answers it needs. */
enum class Question(val minSize: Int, override val label: String): Labeled {
    ValidLocation(1, "Is this a real location?"),
    ValidEvent(1, "Is this a real event?"),
    ValidPost(1, "Does this post belong here?"),
}
