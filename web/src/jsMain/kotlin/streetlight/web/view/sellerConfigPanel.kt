package streetlight.web.view

import kampfire.model.setTrue
import kampfire.model.storeOf
import kampfire.model.toUrl
import koala.modifier.*
import koala.dom.*
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.model.data.CapabilityStatus
import streetlight.web.model.SellerConnector
import streetlight.web.model.SellerStage
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.SellerConfig
import streetlight.web.model.api
import streetlight.web.model.toaster

/** The status of the star's Stripe connection, with a button that opens a dialog to set it up or manage it. */
fun ViewScope.sellerConfigPanel() {
    val connector = SellerConnector(contentScope, api.seller, toaster)
    val config = SellerConfig(contentScope)
    context(connector, config) {
        flowBlock(config.offeringEditState) { edit ->
            when (edit) {
                null -> {
                    formRow {
                        stripeConnectForm()
                        offeringsForm()
                    }
                }
                else -> {
                    // create OfferingEditor as context
                    // show form
                }
            }
        }
    }
}

context(model: SellerConfig)
fun ViewScope.offeringsForm() {

}

fun ViewScope.offeringEditForm() {

}