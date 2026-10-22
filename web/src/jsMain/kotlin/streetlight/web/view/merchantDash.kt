package streetlight.web.view

import kabinet.utils.toAgoFormat
import koala.dom.ViewScope
import koala.dom.component.column
import koala.component.heading5
import koala.dom.component.row
import koala.dom.component.section
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MenuAction
import koala.modifier.*
import streetlight.model.data.toCentsLabel
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.dashEntity
import streetlight.web.dom.layout.dashSection
import streetlight.web.dom.layout.dashSectionHeading
import streetlight.web.model.Merchant

/** The star's offerings, the payments they have requested, and their customers. */
context(model: Merchant)
fun ViewScope.merchantDash() {
    offeringsSection()
    paymentsSection()
    customersSection()
}

/** The star's offerings, each with buttons that stage its edit or an invoice for it, under a button for a new one. */
context(model: Merchant)
private fun ViewScope.offeringsSection() = dashSection("Offerings", listOf(
    MenuAction("New", onClick = model::startNewOffering)
)) {
    flowBlock(model.offeringsState) { offerings ->
        if (offerings == null) return@flowBlock
        if (offerings.isEmpty()) {
            centeredText("Nothing offered yet.")
            return@flowBlock
        }
        column {
            offerings.forEach { offering ->
                dashEntity(offering, listOf(
                    MenuAction("Edit") { model.startOfferingEdit(offering) },
                    MenuAction("send invoice") { model.startPaymentEdit(offering) },
                ))
            }
        }
    }
}

/** The payments the star has requested, newest first, each with its customer, amount, status and age. */
context(model: Merchant)
private fun ViewScope.paymentsSection() = dashSection("Payments") {
    flowBlock(model.paymentsState) { payments ->
        if (payments == null) return@flowBlock
        if (payments.isEmpty()) {
            centeredText("No invoices sent yet.")
            return@flowBlock
        }
        column {
            payments.forEach { payment ->
                row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
                    column(modify(Flex1, Gap0)) {
                        heading5(payment.email.value)
                        textBlock(payment.createdAt.toAgoFormat(), modify(TextSmall, OpacityHalf))
                    }
                    textBlock("$${payment.amountCents.toCentsLabel()}")
                    textBlock(payment.paymentStatus.name, modify(TextUppercase, TextSmall, OpacityHigh))
                }
            }
        }
    }
}

/** The star's customers, each by name and email. */
context(model: Merchant)
private fun ViewScope.customersSection() = dashSection("Customers") {
    flowBlock(model.customersState) { customers ->
        if (customers == null) return@flowBlock
        if (customers.isEmpty()) {
            centeredText("No customers yet.")
            return@flowBlock
        }
        column {
            customers.forEach { customer ->
                row(modify(AlignItemsCenter, ZenBg, Padding(1))) {
                    column(modify(Flex1, Gap0)) {
                        heading5(customer.name ?: customer.email.value)
                        customer.name?.let { textBlock(customer.email.value, modify(TextSmall, OpacityHalf)) }
                    }
                }
            }
        }
    }
}
