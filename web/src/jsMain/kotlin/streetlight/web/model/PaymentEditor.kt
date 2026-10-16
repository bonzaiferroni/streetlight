package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit
import streetlight.web.io.ApiClient

/** Edits [edit], a new payment for one of the star's offerings, and creates it. */
class PaymentEditor(
    edit: PaymentEdit,
    private val api: ApiClient,
) {
    private val state = storeOf(edit)

    val emailField = state.mutableTapOf({ it.email ?: "" }) { copy(email = it.takeIf(String::isNotBlank)) }
    val validityField = state.tapOf { it.validity }

    /** Creates the payment and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Payment? {
        state.now.validity.message?.let {
            messenger.deliver(it)
            return null
        }
        messenger.deliverSending()
        return api.payment.create(state.now).toDataOrNull(messenger)
    }
}
