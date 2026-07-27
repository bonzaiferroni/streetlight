package streetlight.web.ui

import kampfire.api.Username
import kampfire.api.toEmail
import kampfire.api.toValidOutcome
import kampfire.model.LoginRequest
import kampfire.model.handleOutcome
import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.Flex1
import koala.css.MaxWidth24
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.MessageStore
import koala.dom.button
import koala.dom.checkBox
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.lottie
import koala.dom.row
import koala.dom.textBlock
import koala.dom.textField
import koala.model.MutableField
import koala.model.storeOf
import kotlinx.html.InputType
import streetlight.web.model.CredentialStore
import streetlight.web.model.SessionGate
import streetlight.web.model.UserCreator

fun ViewScope.signInForm(model: UserCreator) {
    val gate = app.get<SessionGate>()
    val cred = app.get<CredentialStore>()

    form {
        formRow {
            flowBlock(model.guestField) { username ->
                when (username) {
                    null -> recoverOrSignInForm(cred, gate)
                    else -> guestSignInForm(username, cred, gate)
                }
            }

            column(modify(AlignItemsCenter)) {
                lottie(LottieFile.StrollingMan, modify(MaxWidth24))
            }
        }
    }
}

fun ViewScope.recoverOrSignInForm(cred: CredentialStore, gate: SessionGate) {
    val isRecoveringField = storeOf(false)
    flowBlock(isRecoveringField) { isRecovering ->
        when (isRecovering) {
            true -> form {
                val messages = MessageStore()
                val emailField = storeOf("")
                val isSubmitVisible = storeOf(true)
                formSection("Reset Password") {
                    formText("If you have an email address registered with Streetlight you can reset your password.")
                    textField(emailField)
                    formSubmit("Reset my password", {
                        val email = emailField.now.toEmail().toValidOutcome().handleOutcome(messages) ?: return@formSubmit
                        launchEffect {
                            val unit = api.resetPassword(email).handleOutcome(messages)
                            if (unit != null) {
                                isSubmitVisible.set(false)
                                messages.set("Check your email for a link to reset your password.")
                            }
                        }
                    }, messages, isDisplayedFlow = isSubmitVisible)
                }
            }
            else -> registeredSignInForm(isRecoveringField, cred, gate)
        }
    }
}

fun ViewScope.guestSignInForm(username: Username, cred: CredentialStore, gate: SessionGate) {
    val messages = MessageStore()
    form {
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

fun ViewScope.registeredSignInForm(isRecoveringField: MutableField<Boolean>, cred: CredentialStore, gate: SessionGate) {
    form {
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
                row(modify(AlignItemsCenter)) {
                    checkBox(cred.stayLoggedInField, "Stay signed in", modify(Flex1))
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