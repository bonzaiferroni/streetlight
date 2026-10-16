package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit
import streetlight.model.data.toEdit
import streetlight.web.io.OfferingClient

/**
 * The star's offerings, and the [SellerConfigState.focus] the panel shows in place of them: an [OfferingEdit] for an
 * [OfferingEditor] or a [PaymentEdit] for a [PaymentEditor], `null` while none is staged.
 */
class SellerConfig(
    private val scope: CoroutineScope,
    private val api: OfferingClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SellerConfigState())

    val focusState = state.tapOf { it.focus }
    val offeringsState = state.tapOf { it.offerings }

    init {
        scope.launch("read offerings") {
            val offerings = api.readMine().toDataOr(toaster) { return@launch }
            state.set { copy(offerings = offerings) }
        }
    }

    fun startNewOffering() = state.set { copy(focus = OfferingEdit()) }

    fun startOfferingEdit(offering: Offering) = state.set { copy(focus = offering.toEdit()) }

    fun startPaymentEdit(offering: Offering) = state.set { copy(focus = PaymentEdit(offering.offeringId)) }

    fun cancelFocus() = state.set { copy(focus = null) }

    /** Puts [offering] in the list, in place of the one it updates or last when it is new, and ends the edit. */
    fun finishOfferingEdit(offering: Offering) = state.set {
        val offerings = offerings ?: emptyList()
        val isUpdate = offerings.any { it.offeringId == offering.offeringId }
        copy(
            offerings = if (isUpdate) offerings.map { if (it.offeringId == offering.offeringId) offering else it }
                else offerings + offering,
            focus = null,
        )
    }

    /** Ends the payment edit, announcing the payment [payment] requests. */
    fun finishPaymentEdit(payment: Payment) {
        toaster.toast("Invoice sent to ${payment.email}")
        state.set { copy(focus = null) }
    }
}

data class SellerConfigState(
    val focus: Any? = null,
    val offerings: List<Offering>? = null,
)
