package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.component.textField
import koala.dom.flowValid
import koala.model.MenuAction
import koala.model.MessageStore
import koala.modifier.*
import streetlight.model.data.PaymentProperty
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.PaymentEditor
import streetlight.web.model.Merchant

/** The form of the payment [model] edits, which returns to [config]'s list once sent or abandoned. */
context(model: PaymentEditor, config: Merchant)
fun ViewScope.paymentEditForm() = formColumn {
    val messages = MessageStore()

    formRow {
        formSection("Customer") {
            when (val email = model.customerEmail) {
                null -> textField(model.emailState, "email")
                    .flowValid(PaymentProperty.Email, model.validityField, contentScope)
                else -> textBlock(email)
            }
            when (val name = model.customerName) {
                null -> textField(model.nameState, "name")
                else -> textBlock(name)
            }
        }
        formSection("Invoice") {
            row(AlignItemsCenter) {
                textField(model.priceState, "price", Flex1, prefix = "$")
                    .flowValid(PaymentProperty.Price, model.validityField, contentScope)
                textField(model.platformCutField, "Streetlight cut", Flex1, suffix = "%")
                    .flowValid(PaymentProperty.PlatformCut, model.validityField, contentScope)
            }
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
