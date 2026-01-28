package streetlight.web

import koala.dom.button

fun RenderContext.viewEvent() {
    button("go home") {
        portal.go(AppScreen.Home)
    }
}