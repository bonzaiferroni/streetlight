package streetlight.web.ui

import koala.css.PositionAnchor
import koala.dom.DOMRender
import koala.dom.button
import koala.dom.flowIsWorking
import koala.html.Id
import kotlinx.coroutines.flow.flowOf

fun DOMRender.viewSandbox() {
    button("my button")
        .flowIsWorking(flowOf(true), renderScope)
}

private val menuId = Id("menu")
private val myAnchor = PositionAnchor("my-anchor")