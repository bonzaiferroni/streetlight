package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.textField
import streetlight.web.model.AccountEditor

fun ViewScope.starAccountForm(model: AccountEditor) = form {
    formRow {
        formSection("Identity") {
            textField(model.nameField, "name", maxLength = 50)
            formText("You have the option of sharing your real name.")
        }
        emailFormSection(model.emailEditor)
    }
    formSubmit("save", model::submit, model.messages)
}