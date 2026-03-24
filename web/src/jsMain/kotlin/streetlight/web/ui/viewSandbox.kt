package streetlight.web.ui

import koala.css.PositionAnchorValue
import koala.css.setAnchorName
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.textBlock
import koala.html.Id
import koala.html.TagAttribute
import koala.html.setAttribute
import koala.html.popover
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter

fun RenderContext.viewSandbox(app: Streetlight) {
    column {
        button("open") {
            setAnchorName(myAnchor)
            setAttribute(TagAttribute.popoverTarget, menuId)
        }
        popover(menuId, myAnchor) {
            textBlock("ahoy!")
        }
        appFooter()
    }
}

private val menuId = Id("menu")
private val myAnchor = PositionAnchorValue("my-anchor")