package streetlight.web.standalone

import kampfire.api.Password
import kampfire.api.toValidOutcome
import kampfire.model.Ok
import kampfire.model.Problem
import koala.core.addGlobalFunctions
import koala.css.ErrorFg
import koala.dom.modify
import koala.dom.querySelector
import koala.dom.unmodify
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import streetlight.web.shells.PasswordResetForm

fun main() {
    println("hello passwordReset!")
    addGlobalFunctions(listOf(
        PasswordResetForm.SendReset to ::sendReset
    ))
}

fun sendReset(tokenArg: String) {
    println("sending reset! $tokenArg")
    with (PasswordResetForm) {
        val passwordInput = querySelector(PasswordId) as HTMLInputElement
        val retypeInput = querySelector(RetypeId) as HTMLInputElement
        val password = passwordInput.value.takeIf { it.isNotBlank() } ?: return
        if (password != retypeInput.value) {
            setMessage("Password input does not match.", true)
            return
        }
        when (val outcome = Password(password).toValidOutcome()) {
            is Ok -> setMessage("Sending...")
            is Problem -> setMessage(outcome.message, true)
        }
    }
}

fun setMessage(text: String, isError: Boolean = false) {
    with (PasswordResetForm) {
        val p = querySelector(MessageId) as HTMLParagraphElement
        p.textContent = text
        if (isError) {
            p.modify(ErrorFg)
        } else {
            p.unmodify(ErrorFg)
        }
    }
}