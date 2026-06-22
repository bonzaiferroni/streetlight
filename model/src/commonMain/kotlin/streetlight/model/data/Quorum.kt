package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

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

enum class Question(val minSize: Int) {
    ValidLocation(1),
    ValidEvent(1),
    ValidPost(1),
}