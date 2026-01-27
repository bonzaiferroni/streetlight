package koala.html

import koala.css.CssClass
import kotlinx.html.*
import kotlinx.html.button as buttonCore

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String? = null,
    modifier: CssClass? = null,
    content: BUTTON.() -> Unit = {},
) {
    buttonCore(classes = modifier?.value) {
        onClick?.let { this.onClick = it }
        +text
        content()
    }
}