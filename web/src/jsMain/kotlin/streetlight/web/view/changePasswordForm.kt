package streetlight.web.view

import koala.modifier.*
import koala.dom.*
import kotlinx.html.InputType
import streetlight.model.data.viableEmail
import streetlight.web.model.AccountEditor
import koala.dom.component.*
import koala.dom.layout.*
import koala.model.*
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

context(model: AccountEditor)
fun ViewScope.changePasswordForm() = formSection("Password") {
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
                context(model.passwordEditor) {
                    passwordFormInput()
                }
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
