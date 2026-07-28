package streetlight.web.ui

import kampfire.api.obfuscate
import kampfire.model.UIMessage
import koala.css.AlignItemsCenter
import koala.css.CautionFg
import koala.css.Flex1
import koala.css.ValidFg
import koala.css.Zen
import koala.css.modify
import koala.dom.*
import koala.html.spacer
import streetlight.model.data.EmailStatus
import streetlight.web.model.AccountEditor

fun ViewScope.starAccountForm(model: AccountEditor) = form {
    formRow {
        // formSection("Identity") {
        //     textField(model.nameField, "name", maxLength = 50)
        //     formText("You have the option of sharing your real name.")
        // }

        formSection("Email") {
            flowBlock(model.emailAndStatusField) { (email, status) ->
                console.log(status)
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
                    column {
                        emailFormInput(model.emailEditor)
                        formSubmit("add email", model::addEmail, model.emailMessages)
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
                                        true -> button("Resend Email", model::verifyEmail, modify(Zen))
                                        else -> button("Verify", model::verifyEmail)
                                    }
                                }
                            }
                        }
                        messageBox(model.emailMessages)
                    }
                }
            }
        }

        formSection("Account Security") {
            button("Change Password")
        }

        //formSection("Validate") {
        //    button("Verify email", model::verifyEmail)
        //    // button("Reset Password", model::resetPassword)
        //}
    }
}