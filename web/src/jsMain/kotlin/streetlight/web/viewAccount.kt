package streetlight.web

import koala.css.*
import koala.dom.*
import koala.model.Portal
import koala.model.mapDistinct
import kotlinx.html.InputType
import kotlinx.html.js.p

fun RenderContext.viewAccount(
    app: AppContext,
) {
    val gate = app.gate

    flowBlock(gate.userFlow) { user ->
        column {
            if (user != null) {
                viewUserHub(app, user)
            } else {
                signInFields(app)
            }
        }
    }
}

fun RenderContext.signInFields(
    app: AppContext,
) {
    val gate = app.gate
    val portal = app.portal

    column {
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
            onChangeValue = gate::setUsername,
            placeholder = "username/email",
            binding = gate.stateFlow.mapDistinct { it.usernameText })
        textField(
            label = "password",
            onChangeValue = gate::setPassword,
            placeholder = "password",
            binding = gate.stateFlow.mapDistinct { it.passwordText }) {
            type = InputType.password
        }
        checkBox("Stay signed in", gate::setStayLoggedIn, gate.stateFlow.mapDistinct { it.stayLoggedIn })
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