package streetlight.web

import koala.css.*
import koala.dom.*
import kotlinx.html.InputType
import kotlinx.html.js.p

fun RenderContext.viewAccount(
    app: AppContext,
) {
    val gate = app.gate

    flowBlock(gate.userFlow) { user ->
        if (user != null) {
             viewUserHub(app, user)
        } else {
            column(modify(QueryRow, FlexItems1)) {
                 signInCard(app)
                 createAccountCard(app)
            }
        }
    }
}

fun RenderContext.signInCard(
    app: AppContext,
) {
    val gate = app.gate
    val cred = gate.cred
    val portal = app.portal

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
            binding = cred.usernameFlow
        )
        textField(
            label = "password",
            onChangeValue = cred::setPassword,
            placeholder = "password",
            binding = cred.passwordFlow
        ) {
            type = InputType.password
        }
        checkBox("Stay signed in", cred::setStayLoggedIn, cred.stayLoggedInFlow)
        row {
            button("go home", onClickEvent = {
                portal.go(HomeRoute())
            })
            button("sign in", modifiers = modify(Accent), onClickEvent = {
                gate.signIn()
            })
        }
    }
}