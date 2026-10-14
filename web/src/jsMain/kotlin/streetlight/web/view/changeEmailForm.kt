package streetlight.web.view

import kampfire.api.EmailAddress
import kampfire.api.obfuscate
import koala.modifier.*
import koala.dom.*
import kotlinx.html.InputType
import streetlight.model.data.EmailStatus
import streetlight.web.model.AccountEditor
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

context(model: AccountEditor)
fun ViewScope.changeEmailForm() = formSection("Email") {
    flowBlock(model.emailAndStatusField) { (email, status) ->
        if (status == EmailStatus.NotOwned || status == EmailStatus.Bounced) {
            invalidStatusForm(email)
        } else if (email == null) {
            emailNullForm()
        } else  {
            flowBlock(model.isRemovingEmailField, modify(Magic, Blur)) { isRemovingEmail ->
                when (isRemovingEmail) {
                    true -> removingEmailForm()
                    else -> emailProvidedForm(email, status)
                }
            }
        }
    }
}

context(model: AccountEditor)
private fun ViewScope.emailProvidedForm(
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

context(model: AccountEditor)
private fun ViewScope.emailNullForm() {
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
        context(model.emailEditor) {
            emailFormInput()
        }
        formSubmit(submitLabel, model::addEmail, model.emailMessages)
    }
}

context(model: AccountEditor)
private fun ViewScope.invalidStatusForm(email: EmailAddress?) {
    column {
        textBlock(
            "We were unable to deliver to the email address you provided, please provide a different one.",
            CautionFg
        )
        email?.let {
            textBlock("Provided address: ${email.obfuscate()}")
        }
        context(model.emailEditor) {
            emailFormInput()
        }
        formSubmit("add email", model::addEmail, model.emailMessages)
    }
}

context(model: AccountEditor)
private fun ViewScope.removingEmailForm() {
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
