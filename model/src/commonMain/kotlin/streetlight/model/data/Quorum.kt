package streetlight.model.data

import kampfire.model.Labeled
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

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

enum class Question(val minSize: Int, override val label: String): Labeled {
    ValidLocation(1, "Is this a real location?"),
    ValidEvent(1, "Is this a real event?"),
    ValidPost(1, "Does this post belong here?"),
}
