package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.textField
import koala.dom.flowValid
import koala.dom.launchEffect
import koala.model.MenuAction
import koala.model.MessageStore
import streetlight.model.data.PaymentProperty
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.PaymentEditor
import streetlight.web.model.SellerConfig

/** The form of the payment [model] edits, which returns to [config]'s list once sent or abandoned. */
context(model: PaymentEditor, config: SellerConfig)
fun ViewScope.paymentEditForm() = formColumn {
    val messages = MessageStore()

    formRow {
        formSection("Invoice") {
            textField(model.emailField, "email")
                .flowValid(PaymentProperty.Email, model.validityField, contentScope)
        }
    }
    formSubmit(
        label = "Send",
        onClick = {
            launchEffect {
                val payment = model.submitSuspend(messages) ?: return@launchEffect
                config.finishPaymentEdit(payment)
            }
        },
        messenger = messages,
        back = MenuAction("back", onClick = config::cancelFocus),
    )
}
