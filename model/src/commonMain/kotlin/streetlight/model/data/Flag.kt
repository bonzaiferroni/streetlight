package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class Flag(
    val flagId: FlagId,
    val flaggerId: StarId,
    val policyId: PolicyId?,
    val recordId: Uuid,
    val recordType: RecordType,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class FlagId(override val value: Uuid): RecordId {
    companion object {
        fun random() = FlagId(Uuid.random())
    }
}