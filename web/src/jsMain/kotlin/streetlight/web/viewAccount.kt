package streetlight.web

import koala.dom.button
import kotlinx.html.js.p

fun RenderContext.viewAccount() {
    p {
        +"Hello account!"
    }
    button("go home") {
        portal.go(Home())
    }
}