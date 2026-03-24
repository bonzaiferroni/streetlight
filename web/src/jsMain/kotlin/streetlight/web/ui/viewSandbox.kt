package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.PositionAnchor
import koala.css.SlideLeft
import koala.css.SlideUp
import koala.css.modify
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

fun RenderContext.viewSandbox(app: Streetlight) {
    column {
        button("open") {
            setAnchorName(myAnchor)
            setAttribute(TagAttribute.popoverTarget, menuId)
        }
        popover(menuId, myAnchor, modify(Magic, Blur)) {
            textBlock("ahoy!")
        }
        appFooter()
    }
}

private val menuId = Id("menu")
private val myAnchor = PositionAnchor("my-anchor")