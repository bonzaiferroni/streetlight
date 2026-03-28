package streetlight.web.ui

import koala.LottieFile
import koala.css.*
import koala.dom.*
import koala.html.Id
import kotlinx.html.InputType
import kotlinx.html.js.p
import streetlight.web.HomeRoute
import streetlight.web.model.Streetlight

fun RenderContext.viewAccount(
    app: Streetlight,
) {
    val gate = app.gate

    flowBlock(gate.userFlow) { user ->
        if (user != null) {
             viewUserHub(app, user)
        } else {
            tabs(Id("sign-in-tabs")) {
                tab("Sign in") {
                    signInContent(app)
                }
                tab("Sign up") {
                    createAccountContent(app)
                }
            }
        }
    }
}

fun RenderContext.signInContent(
    app: Streetlight,
) {
    val gate = app.gate
    val cred = gate.cred
    val portal = app.portal

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
                onChangeValue = cred::setUsername,
                placeholder = "username/email",
                bindFlow = cred.usernameFlow
            )
            textField(
                label = "password",
                onChangeValue = cred::setPassword,
                placeholder = "password",
                bindFlow = cred.passwordFlow
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
            lottie(LottieFile.strollingMan, modify(MaxWidth50P))
        }
    }
}