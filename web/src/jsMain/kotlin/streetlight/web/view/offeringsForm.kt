package streetlight.web.view

import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.component.column
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.modifier.*
import streetlight.model.data.priceLabel
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSection
import streetlight.web.model.SellerConfig

/** The star's offerings, each with a button that stages its edit, under a button that stages a new one. */
context(model: SellerConfig)
fun ViewScope.offeringsForm() = formSection("Offerings") {
    button("New offering", model::startNewOffering, AlignSelfCenter)
    flowBlock(model.offeringsState) { offerings ->
        when {
            offerings == null -> Unit
            offerings.isEmpty() -> centeredText("Nothing offered yet.")
            else -> column {
                offerings.forEach { offering ->
                    row(AlignItemsCenter) {
                        textBlock(offering.title, Flex1)
                        textBlock(offering.priceLabel, OpacityHigh)
                        button("Edit", { model.startOfferingEdit(offering) })
                    }
                }
            }
        }
    }
}
