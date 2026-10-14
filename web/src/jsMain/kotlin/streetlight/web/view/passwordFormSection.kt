package streetlight.web.view

import kampfire.api.Password
import koala.dom.ViewScope
import koala.dom.component.textField
import kotlinx.html.InputType
import streetlight.web.model.PasswordEditor
import streetlight.web.dom.layout.formBullets
import streetlight.web.dom.layout.formSection

context(model: PasswordEditor)
fun ViewScope.passwordFormSection() = formSection("Password") {
    passwordFormInput()
}

context(model: PasswordEditor)
fun ViewScope.passwordFormInput() {
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