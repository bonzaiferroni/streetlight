package streetlight.web.ui

import koala.css.TextSmall
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.textField
import streetlight.web.model.EmailEditor

fun ViewScope.emailFormSection(model: EmailEditor) = formSection("Email") {
    emailFormInput(model)
}

fun ViewScope.emailFormInput(model: EmailEditor) {
    textField(model.emailStringField, "optional", placeholder = "email")
    formBullets(
        null,
        "Providing an email address is optional",
        "It can be used to reset your password",
        "Streetlight will never share your email or contact you without your request"
    )
}