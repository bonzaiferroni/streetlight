package streetlight.web.ui

import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.onView
import koala.dom.textBlock
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.style
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter

fun RenderContext.viewSandbox(app: Streetlight) {
    column {
        button("open") {
            attributes["popovertarget"] = "menu"
            style = "anchor-name: --my-anchor;"
        }
        popBox()
        appFooter()
    }
}

fun RenderContext.popBox() {
    div {
        attributes["popover"] = "auto"
        id = "menu"
        +"Ahoy!"
        style = "position-anchor: --my-anchor;"
    }
}
