package streetlight.web.standalone

import kampfire.api.Password
import kampfire.api.obfuscatePassword
import kampfire.api.toValidOutcome
import kampfire.model.Ok
import kampfire.model.PasswordResetRequest
import kampfire.model.Problem
import kampfire.model.Token
import koala.core.addGlobalFunctions
import koala.css.ErrorFg
import koala.dom.modify
import koala.dom.querySelector
import koala.dom.unmodify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLParagraphElement
import streetlight.model.Api
import streetlight.web.shells.PasswordResetForm
import web.http.BodyInit
import web.http.Headers
import web.http.POST
import web.http.RequestInit
import web.http.RequestMethod
import web.http.fetch
import web.http.text
import web.window.window

fun main() {
    println("hello passwordReset")
    addGlobalFunctions(listOf(
        PasswordResetForm.SendReset to ::sendReset
    ))
}

fun sendReset(tokenArg: String) {
    println("sending reset")
    with (PasswordResetForm) {
        val passwordInput = querySelector(PasswordId) as HTMLInputElement
        val retypeInput = querySelector(RetypeId) as HTMLInputElement
        val password = passwordInput.value.takeIf { it.isNotBlank() } ?: return
        if (password != retypeInput.value) {
            setMessage("Password input does not match.", true)
            return
        }
        when (val outcome = Password(password).toValidOutcome()) {
            is Ok -> CoroutineScope(Dispatchers.Main).launch {
                setMessage("Sending...")
                val redemption = PasswordResetRequest(Token(tokenArg), outcome.data.obfuscatePassword())
                val init = RequestInit(
                    method = RequestMethod.POST,
                    headers = Headers().apply {
                        append("Content-Type", "application/json")
                    },
                    body = BodyInit(Json.encodeToString(redemption))
                )
                val response = fetch(Api.AccountAction.ResetPassword.Redemption.path, init)
                if (response.ok) {
                    setMessage("Success!")
                    window.location.replace("/")
                } else {
                    setMessage(response.text(), true)
                }
            }
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