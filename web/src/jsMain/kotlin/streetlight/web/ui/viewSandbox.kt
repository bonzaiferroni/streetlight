package streetlight.web.ui

import koala.css.*
import koala.dom.*
import koala.html.Id
import kotlinx.coroutines.flow.flowOf

fun ScopedDOM.viewSandbox() {
    tabs {
        tab("one") {
            textBlock("the first tab content")
        }
        tab("two") {
            textBlock("the second tab content")
        }
        tab("three") {
            textBlock("the third tab content")
        }
    }
}

private val menuId = Id("menu")
private val myAnchor = PositionAnchor("my-anchor")