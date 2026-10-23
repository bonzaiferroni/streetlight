package streetlight.web.view

import kampfire.model.setTrue
import kampfire.model.storeOf
import kampfire.model.toUrl
import koala.dom.ViewScope
import koala.dom.component.button
import koala.dom.component.column
import koala.dom.component.dialog
import koala.dom.component.dialogContent
import koala.dom.component.row
import koala.dom.component.section
import koala.dom.component.textBlock
import koala.dom.layout.flowBlock
import koala.model.MessageStore
import koala.modifier.AlignItemsCenter
import koala.modifier.AlignSelfCenter
import koala.modifier.Blur
import koala.modifier.CautionFg
import koala.modifier.Flex1
import koala.modifier.Magic
import koala.modifier.ValidFg
import koala.modifier.modify
import streetlight.model.data.CapabilityStatus
import streetlight.web.dom.layout.actionSection
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSubmit
import streetlight.web.model.SellerConnector
import streetlight.web.model.SellerStage

context(model: SellerConnector)
fun ViewScope.stripeConnectForm() {
    actionSection("Stripe") {
        val isDialogOpen = storeOf(false)

        row(AlignItemsCenter) {
            flowBlock(model.stageFlow, modify(Flex1, Magic, Blur)) { stage ->
                textBlock(stage.statusLabel, stage.statusMod)
            }
            button("Manage", isDialogOpen::setTrue)
        }

        dialog(isDialogOpen) {
            dialogContent("Stripe") {
                sellerStageContent()
            }
        }
    }
}

context(model: SellerConnector)
private fun ViewScope.sellerStageContent() {
    val messages = MessageStore()
    flowBlock(model.stageFlow, modify(Magic, Blur)) { stage ->
        val status = model.stateNow.status
        column {
            when (stage) {
                SellerStage.Loading -> Unit
                SellerStage.NotStarted -> {
                    centeredText(sellerInfo)
                    formSubmit("Start selling", { model.onboard(messages) }, messages)
                }
                SellerStage.AwaitingSeller -> {
                    centeredText("Stripe needs a few more details from you.")
                    formSubmit("Continue setup", { model.onboard(messages) }, messages)
                }
                SellerStage.UnderReview -> {
                    centeredText("Stripe is reviewing your details. Check back later.")
                }
                SellerStage.Restricted -> {
                    centeredText("Stripe has restricted payments on your account.", CautionFg)
                    status?.statusReason?.let { centeredText("Reason: $it", CautionFg) }
                    formSubmit("Update details", { model.onboard(messages) }, messages)
                }
                SellerStage.Ready -> {
                    centeredText("Your Stripe account is ready to accept payments.", ValidFg)
                    if (status?.payoutStatus != CapabilityStatus.Active) {
                        centeredText("Payouts are paused until Stripe has what it needs.", CautionFg)
                    }
                    button("Stripe Dashboard", "https://dashboard.stripe.com".toUrl(), AlignSelfCenter)
                }
            }
        }
    }
}

private val sellerInfo = """
Sell the services you offer and get paid through Stripe. Payments go straight to your own Stripe account.
Stripe hosts the setup, so your bank and identity details never touch Streetlight. If you already use Stripe, sign in with the same email to reuse your details.
"""

private val SellerStage.statusLabel get() = when (this) {
    SellerStage.Loading -> "Checking status…"
    SellerStage.NotStarted -> "Not connected"
    SellerStage.AwaitingSeller -> "Setup unfinished"
    SellerStage.UnderReview -> "Under review"
    SellerStage.Restricted -> "Restricted"
    SellerStage.Ready -> "Ready"
}

private val SellerStage.statusMod get() = when (this) {
    SellerStage.Restricted -> CautionFg
    SellerStage.Ready -> ValidFg
    else -> null
}