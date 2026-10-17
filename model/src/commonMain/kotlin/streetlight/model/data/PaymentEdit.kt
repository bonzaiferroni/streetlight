package streetlight.model.data

import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.toValidityCheck
import kotlinx.serialization.Serializable

/**
 * The fields of a payment a form sends, requesting [priceCents] for [offeringId] with Streetlight's [platformCut], in
 * basis points. The payment is from the customer [customerId], or from [email] when it names none, and [name] is
 * given to a customer that has none.
 */
@Serializable
data class PaymentEdit(
    val offeringId: OfferingId,
    val customerId: CustomerId? = null,
    val email: String? = null,
    val name: String? = null,
    val priceCents: Int? = null,
    val platformCut: Int = 0,
) {
    val validity by lazy {
        buildSet {
            if (customerId == null && email?.toEmailAddress()?.toValidOutcome()?.isOk != true) add(PaymentProperty.Email)
            if (priceCents == null || priceCents < 0) add(PaymentProperty.Price)
            if (platformCut !in 0..MAX_PLATFORM_CUT) add(PaymentProperty.PlatformCut)
        }.toValidityCheck()
    }
}

/** The keys of the parts of a [PaymentEdit] its validity checks. */
object PaymentProperty {
    val Email = "email"
    val Price = "price"
    val PlatformCut = "platform cut"
}

/** A new payment for this offering, at its price and cut. */
fun Offering.toPaymentEdit() = PaymentEdit(
    offeringId = offeringId,
    priceCents = priceCents,
    platformCut = platformCut,
)
