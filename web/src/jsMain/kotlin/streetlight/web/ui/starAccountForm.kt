package streetlight.web.ui

import kampfire.api.obfuscate
import koala.css.AlignItemsCenter
import koala.css.AlignSelfCenter
import koala.css.Blur
import koala.css.CautionFg
import koala.css.Magic
import koala.css.Scale
import koala.css.ValidFg
import koala.css.Zen
import koala.css.modify
import koala.dom.*
import kotlinx.html.InputType
import streetlight.model.data.EmailStatus
import streetlight.model.data.viableEmail
import streetlight.web.model.AccountEditor

fun ViewScope.starAccountForm(model: AccountEditor) = form {
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
                    textField(model.passwordNowField, "current password") {
                        type = InputType.password
                    }
                }
                passwordFormInput(model.passwordEditor)
                if (email != null) {
                    button({ model.resetPassword(email, messages) }, modify(AlignSelfCenter)) {
                        textBlock("I forgot my current password.")
                    }
                }
                formSubmit(
                    buttonText = "change",
                    onClick = { model.changePassword(messages) },
                    messages = messages,
                    onCancel = { model.isEditingPasswordField.set(false) }
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
        } else if (email == null) {
            val submitLabel = when (model.stateNow.isChangingEmail) {
                true -> "change email"
                else -> "add email"
            }
            column {
                if (model.stateNow.isChangingEmail && model.hasVerifiedEmail) {
                    textField(model.passwordNowField, "current password") {
                        type = InputType.password
                    }
                }
                emailFormInput(model.emailEditor)
                formSubmit(submitLabel, model::addEmail, model.emailMessages)
            }
        } else  {
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
                    safetyButton("Remove", model::removeEmail, model.emailMessages)
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
    }
}