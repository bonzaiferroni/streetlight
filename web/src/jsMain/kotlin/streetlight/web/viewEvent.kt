package streetlight.web

import koala.dom.RenderContext
import koala.dom.button

fun RenderContext.viewEvent(
    portal: AppPortal,
) {
    button("go home") {
        portal.go(Home())
    }
}