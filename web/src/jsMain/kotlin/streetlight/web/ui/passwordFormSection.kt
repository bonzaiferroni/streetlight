package streetlight.web.ui

import kampfire.api.Password
import koala.dom.ViewScope
import koala.dom.textField
import kotlinx.html.InputType
import streetlight.web.model.PasswordEditor

fun ViewScope.passwordFormSection(model: PasswordEditor) = formSection("Password") {
    passwordFormInput(model)
}

fun ViewScope.passwordFormInput(model: PasswordEditor) {
    textField(model.passwordField, "password") {
        type = InputType.password
    }
    textField(model.confirmationField, "confirm password") {
        type = InputType.password
    }
    formBullets(
        null,
        "Must have at least 3: uppercase, lowercase, number, symbol",
        "Must be at least ${Password.LENGTH_MIN} characters"
    )
}