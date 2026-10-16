package streetlight.web.view

import koala.dom.*
import koala.dom.component.section
import koala.dom.layout.*
import koala.model.MenuAction
import streetlight.web.dom.layout.dashboardHeading
import streetlight.web.model.SellerConnector
import streetlight.web.dom.layout.formRow
import streetlight.web.model.OfferingEditor
import streetlight.web.model.SellerConfig
import streetlight.web.model.api
import streetlight.web.model.toaster

/**
 * The star's Stripe connection and their offerings, or the editor of one offering while [SellerConfig] has an edit
 * staged.
 */
fun ViewScope.sellerConfigPanel() {
    val connector = SellerConnector(contentScope, api.seller, toaster)
    val config = SellerConfig(contentScope, api.offering, toaster)
    context(connector, config) {
        flowBlock(config.offeringEditState) { edit ->
            when (edit) {
                null -> formRow {
                    stripeConnectForm()
                    offeringsDash()
                }
                else -> context(OfferingEditor(edit, api)) {
                    section {
                        val label = if (edit.offeringId == null) "New Offer" else "Update Offer"
                        dashboardHeading(label, listOf(
                            MenuAction("back", onClick = config::cancelOfferingEdit)
                        ))
                        offeringEditForm()
                    }
                }
            }
        }
    }
}
