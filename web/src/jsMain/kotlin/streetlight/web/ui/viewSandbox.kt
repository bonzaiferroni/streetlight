package streetlight.web.ui

import koala.css.Blur
import koala.css.Magic
import koala.css.PositionAnchor
import koala.css.modify
import koala.css.setAnchorName
import koala.dom.RenderContext
import koala.dom.button
import koala.dom.column
import koala.dom.textBlock
import koala.html.Id
import koala.html.Attribute
import koala.html.card
import koala.html.setAttribute
import koala.html.popover
import streetlight.web.model.Streetlight
import streetlight.web.pages.appFooter

fun RenderContext.viewSandbox() {
    textBlock("hello sandbox")
}

private val menuId = Id("menu")
private val myAnchor = PositionAnchor("my-anchor")