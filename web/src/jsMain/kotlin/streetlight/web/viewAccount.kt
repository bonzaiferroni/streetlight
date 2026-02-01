package streetlight.web

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.textField
import kotlinx.html.InputType
import kotlinx.html.input
import kotlinx.html.js.p

fun RenderContext.viewAccount(
    gate: UserGate,
    portal: AppPortal
) {

    column {
        renderState(gate.userFlow) { user ->
            if (user != null) {
                p {
                    +"Hello ${user.username}!"
                }
                button("sign out") {
                    gate.signOut()
                }
            } else {
                column {
                    renderState(gate.messageFlow) { msg ->
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
                    textField(gate::setUsername)
                    textField(gate::setPassword) {
                        type = InputType.password
                    }
                    input {
                        type = InputType.checkBox
                    }
                    button("sign in") {
                        gate.signIn()
                    }
                }
            }
        }
        button("go home") {
            portal.go(Home())
        }
    }
}