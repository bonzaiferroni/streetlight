package streetlight.web.view

import koala.dom.*
import koala.dom.layout.*
import koala.dom.component.column
import koala.model.MenuAction
import koala.model.MessageStore
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.SellerConnector
import streetlight.web.dom.layout.formRow
import streetlight.model.data.OfferingEdit
import streetlight.model.data.PaymentEdit
import streetlight.web.dom.layout.actionSection
import streetlight.web.model.OfferingEditor
import streetlight.web.model.PaymentEditor
import streetlight.web.model.Merchant
import streetlight.web.model.api
import streetlight.web.model.toaster

/**
 * The star's Stripe connection and their offerings, or the editor of the offering or payment [Merchant] has
 * staged as its focus.
 */
fun ViewScope.sellerDash() {
    val connector = SellerConnector(contentScope, api.seller, toaster)
    val config = Merchant(contentScope, api, toaster)
    context(connector, config) {
        flowBlock(config.focusState) { focus ->
            when (focus) {
                null -> formRow {
                    stripeConnectForm()
                    merchantDash()
                }
                is OfferingEdit -> column {
                    val editor = OfferingEditor(focus, contentScope, api, toaster)
                    val messages = MessageStore()
                    context(editor) {
                        val label = if (focus.offeringId == null) "New Offer" else "Update Offer"
                        actionSection(label, listOf(
                            MenuAction("back", onClick = config::cancelFocus)
                        )) {
                            offeringEditForm()
                        }

                        formSubmit(
                            label = "Save",
                            onClick = {
                                launchEffect {
                                    val offering = editor.submitSuspend(messages) ?: return@launchEffect
                                    config.finishOfferingEdit(offering)
                                }
                            },
                            messenger = messages,
                            back = MenuAction("back", onClick = config::cancelFocus),
                        )
                    }
                }
                is PaymentEdit -> column {
                    val editor = PaymentEditor(focus, api)
                    val messages = MessageStore()
                    context(editor) {
                        actionSection("Send Invoice", listOf(
                            MenuAction("back", onClick = config::cancelFocus)
                        )) {
                            paymentEditForm()
                        }

                        formSubmit(
                            label = "Send",
                            onClick = {
                                launchEffect {
                                    val payment = editor.submitSuspend(messages) ?: return@launchEffect
                                    config.finishPaymentEdit(payment)
                                }
                            },
                            messenger = messages,
                            back = MenuAction("back", onClick = config::cancelFocus),
                        )
                    }
                }
                else -> error("unknown seller focus: $focus")
            }
        }
    }
}
