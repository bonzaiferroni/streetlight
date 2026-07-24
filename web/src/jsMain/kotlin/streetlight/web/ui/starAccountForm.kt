package streetlight.web.ui

import koala.dom.ViewScope
import koala.dom.button
import koala.dom.textField
import koala.html.button
import koala.html.column
import streetlight.web.model.AccountEditor

fun ViewScope.starAccountForm(model: AccountEditor) = form {
    formRow {
        formSection("Identity") {
            textField(model.nameField, "name", maxLength = 50)
            formText("You have the option of sharing your real name.")
        }
        emailFormSection(model.emailEditor)
        formSection("Validate") {
            button("Validate email", model::validateEmail)
        }
        column {  }
    }
    formSubmit("save", model::submit, model.messages)
}