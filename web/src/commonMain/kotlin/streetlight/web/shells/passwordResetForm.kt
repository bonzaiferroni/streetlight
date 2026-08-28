package streetlight.web.shells

import kampfire.model.Token
import koala.JsBundle
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.interop.JsSignature
import koala.css.MaxWidth48
import koala.css.MaxWidth64
import koala.css.modify
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

fun FlowContent.passwordResetForm(token: Token) {
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
        linkScript(JsBundle.PasswordReset)
    }
}

object PasswordResetForm {
    val MessageId = Id("password-reset-message")
    val ButtonId = Id("password-reset-button")
    val PasswordId = Id("password-reset-password")
    val RetypeId = Id("password-reset-retype")
    val SendReset = JsSignature("sendReset")
}