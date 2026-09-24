package streetlight.model.data

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A rule of conduct or content, for the whole site or one galaxy, that users can flag records against. */
@Serializable
data class Policy(
    val policyId: PolicyId,
    val policyScope: PolicyScope,
    val policyType: PolicyType,
    val policyTarget: PolicyTarget,
    val label: String,
    val definition: String,
    val isDefault: Boolean,
    val isReportable: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant,
)

@Serializable
@JvmInline
value class PolicyId(override val value: Uuid): RecordId {
    companion object {
        fun random() = PolicyId(Uuid.random())
    }
}