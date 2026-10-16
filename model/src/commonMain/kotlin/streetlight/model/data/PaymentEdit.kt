package streetlight.model.data

import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.toValidityCheck
import kotlinx.serialization.Serializable

/**
 * The fields of a payment a form sends, requesting payment for [offeringId] from the customer [customerId], or from
 * [email] when it names none.
 */
@Serializable
data class PaymentEdit(
    val offeringId: OfferingId,
    val customerId: CustomerId? = null,
    val email: String? = null,
) {
    val validity by lazy {
        buildSet {
            if (customerId == null && email?.toEmailAddress()?.toValidOutcome()?.isOk != true) add(PaymentProperty.Email)
        }.toValidityCheck()
    }
}

/** The keys of the parts of a [PaymentEdit] its validity checks. */
object PaymentProperty {
    val Email = "email"
}
