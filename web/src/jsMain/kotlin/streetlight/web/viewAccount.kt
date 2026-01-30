package streetlight.web

import koala.dom.button
import koala.dom.column
import kotlinx.html.js.p

fun RenderContext.viewAccount() {
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
                p {
                    +"Sign in to continue!"
                }
                button("sign in") {
                    gate.signIn()
                }
            }
        }
        button("go home") {
            portal.go(Home())
        }
    }
}