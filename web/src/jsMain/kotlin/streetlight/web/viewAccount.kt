package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.checkBox
import koala.dom.column
import koala.dom.flowBlock
import koala.dom.textField
import koala.model.Portal
import koala.model.mapDistinct
import kotlinx.html.InputType
import kotlinx.html.js.p

fun RenderContext.viewAccount(
    gate: UserGate,
    portal: Portal
) {

    column {
        flowBlock(gate.userFlow) { user ->
            if (user != null) {
                p {
                    +"Hello ${user.username}!"
                }
                button("sign out", onClickEvent = { gate.signOut() })
            } else {
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
                    button("sign in", onClickEvent = {
                        gate.signIn()
                    })
                }
            }
        }
        button("go home", onClickEvent = {
            portal.go(HomeRoute())
        })
    }
}