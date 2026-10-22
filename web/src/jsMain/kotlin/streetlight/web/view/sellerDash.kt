package streetlight.web.view

import koala.dom.*
import koala.dom.component.section
import koala.dom.layout.*
import koala.model.MenuAction
import streetlight.web.dom.layout.dashSectionHeading
import streetlight.web.model.SellerConnector
import streetlight.web.dom.layout.formRow
import streetlight.model.data.OfferingEdit
import streetlight.model.data.PaymentEdit
import streetlight.web.dom.layout.dashSection
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
                is OfferingEdit -> context(OfferingEditor(focus, api)) {
                    val label = if (focus.offeringId == null) "New Offer" else "Update Offer"
                    dashSection(label, listOf(
                        MenuAction("back", onClick = config::cancelFocus)
                    )) {
                        offeringEditForm()
                    }
                }
                is PaymentEdit -> context(PaymentEditor(focus, api)) {
                    dashSection("Send Invoice", listOf(
                        MenuAction("back", onClick = config::cancelFocus)
                    )) {
                        paymentEditForm()
                    }
                }
                else -> error("unknown seller focus: $focus")
            }
        }
    }
}
