package streetlight.web.view

import koala.SvgFile
import koala.dom.ViewScope
import koala.dom.component.textField
import streetlight.web.model.EmailEditor
import streetlight.web.dom.layout.formBullets
import streetlight.web.dom.layout.formSection

context(model: EmailEditor)
fun ViewScope.emailFormSection() = formSection("Email") {
    emailFormInput()
}

context(model: EmailEditor)
fun ViewScope.emailFormInput() {
    textField(model.emailStringField, "optional", placeholder = "email", icon = SvgFile.Mail)
    formBullets(
        null,
        "Providing an email address is optional",
        "It can be used to reset your password",
        "Streetlight will never share your email or contact you without your request"
    )
}