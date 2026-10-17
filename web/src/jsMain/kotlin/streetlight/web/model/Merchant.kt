package streetlight.web.model

import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOr
import koala.utility.launch
import kotlinx.coroutines.CoroutineScope
import streetlight.model.data.Customer
import streetlight.model.data.Offering
import streetlight.model.data.OfferingEdit
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit
import streetlight.model.data.toPaymentEdit
import streetlight.model.data.toEdit
import streetlight.web.io.ApiClient

/**
 * The star's offerings, payments and customers, and the [SellerConfigState.focus] the panel shows in place of them: an [OfferingEdit] for an
 * [OfferingEditor] or a [PaymentEdit] for a [PaymentEditor], `null` while none is staged.
 */
class Merchant(
    private val scope: CoroutineScope,
    private val api: ApiClient,
    private val toaster: Toaster,
) {
    private val state = storeOf(SellerConfigState())

    val focusState = state.tapOf { it.focus }
    val offeringsState = state.tapOf { it.offerings }
    val paymentsState = state.tapOf { it.payments }
    val customersState = state.tapOf { it.customers }

    init {
        scope.launch("read offerings") {
            val offerings = api.offering.readMine().toDataOr(toaster) { return@launch }
            state.set { copy(offerings = offerings) }
        }
        scope.launch("read payments") {
            val payments = api.payment.readMine().toDataOr(toaster) { return@launch }
            state.set { copy(payments = payments) }
        }
        readCustomers()
    }

    fun startNewOffering() = state.set { copy(focus = OfferingEdit()) }

    fun startOfferingEdit(offering: Offering) = state.set { copy(focus = offering.toEdit()) }

    fun startPaymentEdit(offering: Offering) = state.set { copy(focus = offering.toPaymentEdit()) }

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

    /** Ends the payment edit, announcing [payment] and putting it first in the list. */
    fun finishPaymentEdit(payment: Payment) {
        toaster.toast("Invoice sent to ${payment.email}")
        state.set { copy(payments = listOf(payment) + payments.orEmpty(), focus = null) }
        readCustomers()
    }

    private fun readCustomers() = scope.launch("read customers") {
        val customers = api.customer.readMine().toDataOr(toaster) { return@launch }
        state.set { copy(customers = customers) }
    }
}

data class SellerConfigState(
    val focus: Any? = null,
    val offerings: List<Offering>? = null,
    val payments: List<Payment>? = null,
    val customers: List<Customer>? = null,
)
