package streetlight.web.model

import kampfire.model.Messenger
import kampfire.model.mutableTapOf
import kampfire.model.storeOf
import kampfire.model.tapOf
import kampfire.model.toDataOrNull
import streetlight.model.data.Payment
import streetlight.model.data.PaymentEdit
import streetlight.model.data.toCentsLabel
import streetlight.model.data.toCentsOrNull
import streetlight.model.data.toPercentLabel
import streetlight.web.io.ApiClient

/**
 * Edits [edit], a new payment for one of the star's offerings, and creates it. A customer's email and name that
 * [edit] already holds are shown, not edited.
 */
class PaymentEditor(
    edit: PaymentEdit,
    private val api: ApiClient,
) {
    private val state = storeOf(PaymentEditorState(
        edit = edit,
        priceText = edit.priceCents?.toCentsLabel() ?: "",
        platformCutText = edit.platformCut.toPercentLabel(),
    ))

    /** The customer's email, when [edit] names a customer that has one. */
    val customerEmail = edit.email.takeIf { edit.customerId != null }
    /** The customer's name, when [edit] names a customer that has one. */
    val customerName = edit.name.takeIf { edit.customerId != null }

    val editState = state.mutableTapOf({ it.edit }) { copy(edit = it) }
    val emailState = editState.mutableTapOf({ it.email ?: "" }) { copy(email = it.takeIf(String::isNotBlank)) }
    val nameState = editState.mutableTapOf({ it.name ?: "" }) { copy(name = it.takeIf(String::isNotBlank)) }
    val priceState = state.mutableTapOf({ it.priceText }) {
        copy(priceText = it, edit = this.edit.copy(priceCents = it.toCentsOrNull()))
    }
    val platformCutField = state.mutableTapOf({ it.platformCutText }) {
        copy(platformCutText = it, edit = this.edit.copy(platformCut = it.toPlatformCut()))
    }
    val validityField = editState.tapOf { it.validity }

    /** Creates the payment and returns it, or `null` when invalid or failed. */
    suspend fun submitSuspend(messenger: Messenger): Payment? {
        editState.now.validity.message?.let {
            messenger.deliver(it)
            return null
        }
        messenger.deliverSending()
        return api.payment.create(editState.now).toDataOrNull(messenger)
    }
}

data class PaymentEditorState(
    val edit: PaymentEdit,
    val priceText: String,
    val platformCutText: String,
)
