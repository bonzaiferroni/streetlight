package streetlight.web.ui

import kampfire.model.LoginRequest
import koala.LottieFile
import koala.css.AlignItemsCenter
import koala.css.MaxWidth24
import koala.css.modify
import koala.dom.ViewScope
import koala.dom.MessageStore
import koala.dom.checkBox
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.lottie
import koala.dom.textBlock
import koala.dom.textField
import kotlinx.html.InputType
import streetlight.web.model.CredentialStore
import streetlight.web.model.StarSession
import streetlight.web.model.UserCreator

fun ViewScope.signInForm(model: UserCreator) {
    val gate = app.get<StarSession>()
    val cred = app.get<CredentialStore>()
    val messages = MessageStore()

    form {
        flowBlock(model.guestFlow) { username ->
            when (username) {
                null -> form {
                    formSection("sign in") {
                        column {
                            textField(
                                label = "username/email",
                                onValue = cred::setUsername,
                                placeholder = "username/email",
                                flow = cred.usernameFlow
                            )
                            textField(
                                label = "password",
                                onValue = cred::setPassword,
                                placeholder = "password",
                                flow = cred.passwordFlow
                            ) {
                                type = InputType.password
                            }
                            checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
                        }
                    }
                    formSubmit("Sign in", {
                        val request = cred.getLoginRequest() ?: return@formSubmit
                        gate.signIn(request, messages)
                    }, messages)
                }
                else -> form {
                    formSection("guest sign in") {
                        textBlock("There is a guest account registered on this device: $username")
                        checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
                    }
                    formSubmit("Sign in", {
                        val request = LoginRequest(username.value, cred.stateNow.stayLoggedIn)
                        gate.signIn(request, messages)
                    }, messages)
                }
            }
        }

        column(modify(AlignItemsCenter)) {
            lottie(LottieFile.StrollingMan, modify(MaxWidth24))
        }
    }
}


