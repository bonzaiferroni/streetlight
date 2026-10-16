package streetlight.model.data

import kampfire.api.EmailAddress
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.time.Instant
import kotlin.uuid.Uuid

/** A payment a seller requests for [offeringId] from [email], the address of [customerId], at [amountCents] with Streetlight's [platformFeeCents]. */
@Serializable
data class Payment(
    val paymentId: PaymentId,
    val offeringId: OfferingId,
    val customerId: CustomerId?,
    val email: EmailAddress,
    val amountCents: Int,
    val platformFeeCents: Int,
    val paymentStatus: PaymentStatus,
    val fulfillmentStatus: FulfillmentStatus,
    val dueAt: Instant?,
    val paidAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@JvmInline
@Serializable
value class PaymentId(override val value: Uuid): RecordId {
    override fun toString() = value.toString()
}

/** The state of payment on a payment, as the provider reports it. */
enum class PaymentStatus {
    Draft,
    Open,
    Paid,
    Void,
    Refunded,
    Disputed,
    Uncollectible,
}

/** The state of the seller's delivery on a payment. */
enum class FulfillmentStatus {
    Open,
    InProgress,
    Delivered,
    Canceled,
}
