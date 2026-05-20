package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import kotlinx.html.InputType
import kotlinx.html.js.p
import streetlight.web.HomeRoute
import streetlight.web.model.CredentialStore
import streetlight.web.model.UserGate

fun RenderContext.viewStarDash() {
    val gate = app.get<UserGate>()

    flowBlock(gate.starFlow) { user ->
        if (user != null) {
             viewStarDash(user)
        } else {
            tabs(Id("sign-in-tabs")) {
                tab("Sign in") {
                    signInContent()
                }
                tab("Sign up") {
                    createAccountContent()
                }
            }
        }
    }
}

fun RenderContext.signInContent() {
    val gate = app.get<UserGate>()
    val cred = app.get<CredentialStore>()

    column(modify(MediaMdRow, FlexItems1)) {
        card {
            flowBlock(gate.messageFlow) { msg ->
                if (msg == null) {
                    p {
                        +"Sign in to continue!"
                    }
                } else {
                    p {
                        +msg
                    }
                }
            }
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
            row {
                button("go home", onClickEvent = {
                    portal.go(HomeRoute)
                })
                button("sign in", modifiers = modify(Accent), onClickEvent = {
                    gate.signIn()
                })
            }
        }
        column(modify(AlignItemsCenter)) {
            lottie(LottieFile.StrollingMan, modify(MaxWidth50P))
        }
    }
}