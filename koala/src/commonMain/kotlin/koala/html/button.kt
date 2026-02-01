package koala.html

import koala.css.ButtonClass
import koala.css.CssClass
import koala.css.modify
import kotlinx.html.*
import kotlinx.html.button as buttonCore

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String? = null,
    vararg modifiers: CssClass,
    content: BUTTON.() -> Unit = {},
) {
    buttonCore {
        modify(ButtonClass, *modifiers)
        onClick?.let { this.onClick = it }
        +text
        content()
    }
}