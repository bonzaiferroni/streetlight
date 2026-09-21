package streetlight.web.ui

import kampfire.api.EmailAddress
import kampfire.api.obfuscate
import kampfire.model.toUrl
import koala.modifier.*
import koala.dom.*
import koala.html.Id
import koala.html.listItem
import koala.html.olist
import kotlinx.html.InputType
import kotlinx.html.ol
import streetlight.model.data.EmailStatus
import streetlight.model.data.viableEmail
import streetlight.web.model.AccountEditor
import streetlight.web.model.AppConnector

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
}

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
                    centeredText("New password:", modify(MarginTop(1)))
                }
                passwordFormInput(model.passwordEditor)
                if (email != null) {
                    button({ model.resetPassword(email, messages) }, modify(AlignSelfCenter)) {
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
            column(modify(AlignItemsCenter)) {
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
    column(modify(AlignItemsCenter)) {
        row(modify(AlignItemsCenter)) {
            textBlock(email.obfuscate())
            when (status) {
                EmailStatus.Verified -> textBlock("(Verified)", modify(ValidFg))
                else -> textBlock("(Unverified)", modify(CautionFg))
            }
        }
        row {
            button("Remove", { model.isRemovingEmailField.set(true) }, modify(Zen))
            button("Change", {
                model.changeEmail()
            }, modify(Zen))
            if (isUnverified) {
                flowBlock(model.isVerifySentField) { isSent ->
                    when (isSent) {
                        true -> button("Resend Email", model::verifyExistingEmail, modify(Zen))
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
            centeredText("What is your new email?", modify(MarginTop(1)))
        }
        emailFormInput(model.emailEditor)
        formSubmit(submitLabel, model::addEmail, model.emailMessages)
    }
}

private fun ViewScope.invalidStatusForm(model: AccountEditor, email: EmailAddress?) {
    column {
        textBlock(
            "We were unable to deliver to the email address you provided, please provide a different one.",
            modify(CautionFg)
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