package streetlight.web.view

import kampfire.api.Username
import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.LoginRequest
import kampfire.model.toDataOr
import koala.LottieFile
import koala.modifier.*
import koala.dom.ViewScope
import koala.model.MessageStore
import koala.dom.component.button
import koala.dom.component.checkBox
import koala.dom.component.column
import koala.dom.layout.flowBlock
import koala.dom.component.lottie
import koala.dom.component.row
import koala.dom.component.textBlock
import koala.dom.component.textField
import kampfire.model.MutableTap
import kampfire.model.storeOf
import kotlinx.html.InputType
import streetlight.web.model.CredentialStore
import streetlight.web.model.SessionClient
import streetlight.web.model.UserCreator
import streetlight.web.model.api
import streetlight.web.dom.layout.centeredText
import streetlight.web.dom.layout.formColumn
import streetlight.web.dom.layout.formRow
import streetlight.web.dom.layout.formSection
import streetlight.web.dom.layout.formSubmit

context(model: UserCreator)
fun ViewScope.signInForm() {
    val gate = app.get<SessionClient>()
    val cred = app.get<CredentialStore>()

    formColumn {
        formRow {
            flowBlock(model.guestField) { username ->
                context(cred) {
                    context(gate) {
                        when (username) {
                            null -> recoverOrSignInForm()
                            else -> guestSignInForm(username)
                        }
                    }
                }
            }
        }
    }
}

context(cred: CredentialStore, gate: SessionClient)
fun ViewScope.recoverOrSignInForm() {
    val isRecoveringField = storeOf(false)
    flowBlock(isRecoveringField) { isRecovering ->
        when (isRecovering) {
            true -> formColumn {
                val messages = MessageStore()
                val emailField = storeOf("")
                val isSubmitVisible = storeOf(true)
                formSection("Reset Password") {
                    centeredText("If you have an email address registered with Streetlight you can reset your password.")
                    textField(emailField)
                    formSubmit("Reset my password", {
                        val email = emailField.now.toEmailAddress().toValidOutcome().toDataOr(messages) { return@formSubmit }
                        launchEffect {
                            api.accountAction.resetPassword(email).toDataOr(messages) { return@launchEffect }
                            isSubmitVisible.set(false)
                            messages.set("Check your email for a link to reset your password.")
                        }
                    }, messages, isDisplayedFlow = isSubmitVisible)
                }
            }
            else -> registeredSignInForm(isRecoveringField)
        }
    }
}

context(cred: CredentialStore, gate: SessionClient)
fun ViewScope.guestSignInForm(username: Username) {
    val messages = MessageStore()
    formColumn {
        formSection("guest sign in") {
            textBlock("There is a guest account registered on this device: $username")
            checkBox(cred.stayLoggedInField, "Stay signed in")
        }
        formSubmit("Sign in", {
            val request = LoginRequest(username.value, cred.stateNow.stayLoggedIn)
            gate.signIn(request, messages)
        }, messages)
    }
}

context(cred: CredentialStore, gate: SessionClient)
fun ViewScope.registeredSignInForm(isRecoveringField: MutableTap<Boolean>) {
    formColumn {
        val messages = MessageStore()
        formSection("sign in") {
            column {
                textField(
                    field = cred.usernameField,
                    label = "username/email",
                    placeholder = "username/email",
                )
                textField(
                    label = "password",
                    placeholder = "password",
                    field = cred.passwordField
                ) {
                    type = InputType.password
                }
                row(AlignItemsCenter) {
                    checkBox(cred.stayLoggedInField, "Stay signed in", Flex1)
                    button({ isRecoveringField.set(true) }) {
                        textBlock("I forgot")
                    }
                }
            }
        }
        formSubmit("Sign in", {
            val request = cred.getLoginRequest() ?: return@formSubmit
            gate.signIn(request, messages)
        }, messages)
    }
}