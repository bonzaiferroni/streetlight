package streetlight.web.shells

import kampfire.model.Token
import koala.PageResource
import koala.modifier.AlignItemsCenter
import koala.modifier.Flex1
import koala.interop.JsSignature
import koala.modifier.MaxWidth48
import koala.modifier.MaxWidth64
import koala.modifier.modify
import koala.html.Id
import koala.html.button
import koala.html.column
import koala.html.linkScript
import koala.html.row
import koala.html.setId
import koala.html.textBlock
import koala.html.textField
import kotlinx.html.FlowContent
import kotlinx.html.InputType
import kotlinx.html.onClick

fun FlowContent.passwordResetForm(token: Token, resource: PageResource) {
    column(modify(MaxWidth64)) {
        column(modify(MaxWidth48)) {
            textField("password", id = PasswordResetForm.PasswordId) {
                type = InputType.password
            }
            textField("retype password", id = PasswordResetForm.RetypeId) {
                type = InputType.password
            }
        }
        row(modify(AlignItemsCenter)) {
            textBlock("", modify(Flex1)) {
                setId(PasswordResetForm.MessageId)
            }
            button("Reset My Password") {
                onClick = PasswordResetForm.SendReset.invokeJs(token.toString())
            }
        }
        linkScript(resource.bundle.passwordReset)
    }
}

object PasswordResetForm {
    val MessageId = Id("password-reset-message")
    val ButtonId = Id("password-reset-button")
    val PasswordId = Id("password-reset-password")
    val RetypeId = Id("password-reset-retype")
    val SendReset = JsSignature("sendReset")
}