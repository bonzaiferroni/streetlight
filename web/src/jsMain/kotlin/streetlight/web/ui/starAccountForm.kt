package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.textField
import streetlight.web.model.StarEditor

fun ViewScope.starAccountForm(model: StarEditor) = form {
    formRow {
        formSection("Identity") {
            textField(model.nameField, "name", maxLength = 50)
            formText("You have the option of sharing your real name.")
        }
        emailFormSection(model.emailEditor)
    }
    formSubmit("save", model::submit, model.messages)
}