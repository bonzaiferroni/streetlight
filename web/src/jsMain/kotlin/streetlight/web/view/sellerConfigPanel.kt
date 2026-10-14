package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
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
                    offeringsForm()
                }
                else -> context(OfferingEditor(edit, api)) {
                    offeringEditForm()
                }
            }
        }
    }
}
