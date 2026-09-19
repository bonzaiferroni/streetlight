package streetlight.web.ui

import kampfire.api.Username
import kampfire.api.toEmailAddress
import kampfire.api.toValidOutcome
import kampfire.model.LoginRequest
import kampfire.model.toDataOr
import koala.LottieFile
import koala.modifier.*
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
import kampfire.model.MutableTap
import kampfire.model.storeOf
import kotlinx.html.InputType
import streetlight.web.model.CredentialStore
import streetlight.web.model.SessionClient
import streetlight.web.model.UserCreator

fun ViewScope.signInForm(model: UserCreator) {
    val gate = app.get<SessionClient>()
    val cred = app.get<CredentialStore>()

    formColumn {
        formRow {
            flowBlock(model.guestField) { username ->
                when (username) {
                    null -> recoverOrSignInForm(cred, gate)
                    else -> guestSignInForm(username, cred, gate)
                }
            }

            column(modify(AlignItemsCenter)) {
                lottie(LottieFile.StrollingMan, modify(MaxWidth(24)))
            }
        }
    }
}

fun ViewScope.recoverOrSignInForm(cred: CredentialStore, gate: SessionClient) {
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
            else -> registeredSignInForm(isRecoveringField, cred, gate)
        }
    }
}

fun ViewScope.guestSignInForm(username: Username, cred: CredentialStore, gate: SessionClient) {
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

fun ViewScope.registeredSignInForm(isRecoveringField: MutableTap<Boolean>, cred: CredentialStore, gate: SessionClient) {
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