package streetlight.web.ui

import koala.css.PositionAnchorValue
import koala.css.StyleProperty
import koala.css.styleOf
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.onView
import koala.dom.textBlock
import koala.html.Id
import koala.html.popover
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.style
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter

fun RenderContext.viewSandbox(app: Streetlight) {
    column {
        button("open", styles = styleOf(StyleProperty.anchorName to SandboxValues.myAnchor)) {
            attributes["popovertarget"] = "menu"
        }
        popover(SandboxValues.menuId, SandboxValues.myAnchor) {
            textBlock("ahoy!")
        }
        appFooter()
    }
}

object SandboxValues {
    val menuId = Id("menu")
    val myAnchor = PositionAnchorValue("my-anchor")
}