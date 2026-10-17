package streetlight.model.data

import kampfire.api.EmailAddress
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A buyer a seller has invoiced, [name] at [email]. */
@Serializable
data class Customer(
    val customerId: CustomerId,
    val email: EmailAddress,
    val name: String?,
    val createdAt: Instant,
)

@JvmInline
@Serializable
value class CustomerId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()

    companion object {
        fun random() = CustomerId(Uuid.random())
    }
}
