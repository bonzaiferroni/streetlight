package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.component.column
import koala.dom.component.row
import koala.dom.component.section
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import streetlight.model.data.priceLabel
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.dashboardEntity
import streetlight.web.dom.layout.dashboardHeading
import streetlight.web.model.SellerConfig

/** The star's offerings, each with a button that stages its edit, under a button that stages a new one. */
context(model: SellerConfig)
fun ViewScope.offeringsDash() {
    section {
        dashboardHeading("Offerings", listOf(
            MenuAction("New", onClick = model::startNewOffering)
        ))
        flowBlock(model.offeringsState) { offerings ->
            if (offerings == null) return@flowBlock
            if (offerings.isEmpty()) {
                centeredText("Nothing offered yet.")
                return@flowBlock
            }
            column {
                offerings.forEach { offering ->
                    dashboardEntity(offering, listOf(
                        MenuAction("Edit") { model.startOfferingEdit(offering) }
                    ))
                }
            }
        }
    }
}
