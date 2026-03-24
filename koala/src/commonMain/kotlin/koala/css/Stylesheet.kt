package koala.css

import kotlinx.html.CoreAttributeGroupFacade
import kotlinx.html.FlowContent
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.style

fun FlowContent.setStylesheet(value: String) {
    style {
        +value
    }
}