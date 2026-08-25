package streetlight.web.ui

import kampfire.api.EmailAddress
import kampfire.api.obfuscate
import koala.css.AlignItemsCenter
import koala.css.AlignSelfCenter
import koala.css.Blur
import koala.css.CautionFg
import koala.css.Magic
import koala.css.MarginTop1
import koala.css.Scale
import koala.css.ValidFg
import koala.css.Zen
import koala.css.modify
import koala.dom.*
import kotlinx.html.InputType
import streetlight.model.data.EmailStatus
import streetlight.model.data.viableEmail
import streetlight.web.model.AccountEditor

fun ViewScope.updateAccountForm(model: AccountEditor) = formColumn {
    formRow {
        // formSection("Identity") {
        //     textField(model.nameField, "name", maxLength = 50)
        //     formText("You have the option of sharing your real name.")
        // }

        emailSection(model)

        passwordSection(model)

        //formSection("Validate") {
        //    button("Verify email", model::verifyEmail)
        //    // button("Reset Password", model::resetPassword)
        //}
    }
}

fun ViewScope.passwordSection(model: AccountEditor) = formSection("Password") {
    val messages = MessageStore()

    flowBlock(model.isEditingPasswordField, modify(Magic, Blur, Scale)) { isEditing ->
        if (isEditing) {
            val email = model.stateNow.account.viableEmail
            column {
                if (email != null) {
                    formText("Please provide your current password to authorize change.")
                    textField(model.verifyPasswordField, "current password") {
                        type = InputType.password
                    }
                    formText("New password:", modify(MarginTop1))
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
            formText("Please provide a password to authorize change.")
            textField(model.verifyPasswordField, "current password") {
                type = InputType.password
            }
            formText("What is your new email?", modify(MarginTop1))
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
            formText("Please provide a password to authorize removal.")
            textField(model.verifyPasswordField, "current password") {
                type = InputType.password
            }
        } else {
            formText("Please confirm that you want to remove the email address from this account.")
        }
        formSubmit("Remove email", model::removeEmail, model.emailMessages)
    }
}