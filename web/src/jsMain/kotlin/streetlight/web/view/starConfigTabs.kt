package streetlight.web.view

import kampfire.api.EmailAddress
import kampfire.api.obfuscate
import kampfire.model.toUrl
import koala.modifier.*
import koala.dom.*
import koala.page.Id
import koala.component.listItem
import koala.component.olist
import kotlinx.html.InputType
import kotlinx.html.ol
import streetlight.model.data.EmailStatus
import streetlight.model.data.viableEmail
import streetlight.web.model.AccountEditor
import streetlight.web.model.AppConnector
import streetlight.model.data.CapabilityStatus
import streetlight.web.model.SellerEditor
import streetlight.web.model.SellerStage
import streetlight.web.model.api
import streetlight.web.model.toaster
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

fun ViewScope.starConfigTabs(model: AccountEditor) = tabs(Id("star-config-tabs")) {
    tab("account") {
        formRow {
            emailSection(model)
            passwordSection(model)
        }
    }
    tab("connect apps") {
        val model = AppConnector()
        formRow {
            formSection("PayPal") {
                olist {
                    listItem {
                        +"Visit "
                        navigation("https://paypal.me", "paypal.me")
                        +" to create a public link"
                    }
                    listItem {
                        +"Enter the username you chose in the field below."
                    }
                    listItem {
                        +"Payments go directly to you, Streetlight just provides the link."
                    }
                }
                row {
                    textField(model.paypalIdState, "PayPal username", Flex1)
                    flowBlock(model.paypalIdState) { paypalId ->
                        if (paypalId.isEmpty()) {
                            button("test link", null, Attribute.Disabled.to(Unit))
                        } else {
                            val url = "https://paypal.me/${paypalId}".toUrl()
                            button("Test Link", url)
                        }
                    }
                }
            }
            formSection("Venmo") {
                row {
                    textField(model.venmoIdState, "Venmo", Flex1)
                    flowBlock(model.venmoIdState) { venmoId ->
                        if (venmoId.isEmpty()) {
                            button("test link", null, Attribute.Disabled.to(Unit))
                        } else {
                            val url = "https://venmo.com/${venmoId}".toUrl()
                            button("Test Link", url)
                        }
                    }
                }
            }
            formSection("Cash App") {
                row {
                    textField(model.cashAppIdState, "Cash App", Flex1)
                    flowBlock(model.cashAppIdState) { cashAppId ->
                        if (cashAppId.isEmpty()) {
                            button("test link", null, Attribute.Disabled.to(Unit))
                        } else {
                            val url = "https://cash.app/${cashAppId}".toUrl()
                            button("Test Link", url)
                        }
                    }
                }
            }
        }
    }
    tab("selling") {
        val model = SellerEditor(contentScope, api.seller, toaster)
        formRow {
            sellerSection(model)
        }
    }
}

fun ViewScope.sellerSection(model: SellerEditor) = formSection("Stripe") {
    val messages = MessageStore()
    flowBlock(model.stageFlow, modify(Magic, Blur)) { stage ->
        val seller = model.stateNow.seller
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
                    seller?.statusReason?.let { centeredText("Reason: $it", CautionFg) }
                    formSubmit("Update details", { model.onboard(messages) }, messages)
                }
                SellerStage.Ready -> {
                    centeredText("Your Stripe account is ready to accept payments.", ValidFg)
                    if (seller?.payoutStatus != CapabilityStatus.Active) {
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

fun ViewScope.passwordSection(model: AccountEditor) = formSection("Password") {
    val messages = MessageStore()

    flowBlock(model.isEditingPasswordField, modify(Magic, Blur, Scale)) { isEditing ->
        if (isEditing) {
            val email = model.stateNow.account.viableEmail
            column {
                if (email != null) {
                    centeredText("Please provide your current password to authorize change.")
                    textField(model.verifyPasswordField, "current password") {
                        type = InputType.password
                    }
                    centeredText("New password:", MarginTop(1))
                }
                passwordFormInput(model.passwordEditor)
                if (email != null) {
                    button({ model.resetPassword(email, messages) }, AlignSelfCenter) {
                        textBlock("I forgot my current password.")
                    }
                }
                formSubmit(
                    label = "change",
                    onClick = { model.changePassword(messages) },
                    messenger = messages,
                    back = MenuAction("back") { model.isEditingPasswordField.set(false) }
                )
            }
        } else {
            column(AlignItemsCenter) {
                button("change password", {
                    model.isEditingPasswordField.set(true)
                    messages.clear()
                })
                messageBox(messages)
            }
        }
    }
}

fun ViewScope.emailSection(model: AccountEditor) = formSection("Email") {
    flowBlock(model.emailAndStatusField) { (email, status) ->
        if (status == EmailStatus.NotOwned || status == EmailStatus.Bounced) {
            invalidStatusForm(model, email)
        } else if (email == null) {
            emailNullForm(model)
        } else  {
            flowBlock(model.isRemovingEmailField, modify(Magic, Blur)) { isRemovingEmail ->
                when (isRemovingEmail) {
                    true -> removingEmailForm(model)
                    else -> emailProvidedForm(model, email, status)
                }
            }
        }
    }
}

private fun ViewScope.emailProvidedForm(
    model: AccountEditor,
    email: EmailAddress,
    status: EmailStatus?,
) {
    val isUnverified = status == null || status == EmailStatus.Unverified
    column(AlignItemsCenter) {
        row(AlignItemsCenter) {
            textBlock(email.obfuscate())
            when (status) {
                EmailStatus.Verified -> textBlock("(Verified)", ValidFg)
                else -> textBlock("(Unverified)", CautionFg)
            }
        }
        row {
            button("Remove", { model.isRemovingEmailField.set(true) }, Zen)
            button("Change", {
                model.changeEmail()
            }, Zen)
            if (isUnverified) {
                flowBlock(model.isVerifySentField) { isSent ->
                    when (isSent) {
                        true -> button("Resend Email", model::verifyExistingEmail, Zen)
                        else -> button("Verify", model::verifyExistingEmail)
                    }
                }
            }
        }
        messageBox(model.emailMessages)
    }
}

private fun ViewScope.emailNullForm(model: AccountEditor) {
    val submitLabel = when (model.stateNow.isChangingEmail) {
        true -> "change email"
        else -> "add email"
    }
    column {
        if (model.stateNow.isChangingEmail && model.stateNow.hasVerifiedEmail) {
            centeredText("Please provide a password to authorize change.")
            textField(model.verifyPasswordField, "current password") {
                type = InputType.password
            }
            centeredText("What is your new email?", MarginTop(1))
        }
        emailFormInput(model.emailEditor)
        formSubmit(submitLabel, model::addEmail, model.emailMessages)
    }
}

private fun ViewScope.invalidStatusForm(model: AccountEditor, email: EmailAddress?) {
    column {
        textBlock(
            "We were unable to deliver to the email address you provided, please provide a different one.",
            CautionFg
        )
        email?.let {
            textBlock("Provided address: ${email.obfuscate()}")
        }
        emailFormInput(model.emailEditor)
        formSubmit("add email", model::addEmail, model.emailMessages)
    }
}

private fun ViewScope.removingEmailForm(model: AccountEditor) {
    column {
        if (model.stateNow.hasVerifiedEmail) {
            centeredText("Please provide a password to authorize removal.")
            textField(model.verifyPasswordField, "current password") {
                type = InputType.password
            }
        } else {
            centeredText("Please confirm that you want to remove the email address from this account.")
        }
        formSubmit("Remove email", model::removeEmail, model.emailMessages)
    }
}