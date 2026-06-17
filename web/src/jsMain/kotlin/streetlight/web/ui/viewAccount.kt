package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import kotlinx.html.InputType
import kotlinx.html.js.p
import streetlight.web.HomeRoute
import streetlight.web.model.CredentialStore
import streetlight.web.model.StarSession

fun AppScope.viewStarDash() {
    val gate = app.get<StarSession>()

    flowBlock(gate.starFlow) { user ->
        if (user != null) {
             viewStarDash(user)
        } else {
            tabs(Id("sign-in-tabs")) {
                tab("Sign in") {
                    signInContent()
                }
                tab("Sign up") {
                    fullRegistrationForm()
                }
            }
        }
    }
}

fun AppScope.signInContent() {
    val gate = app.get<StarSession>()
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