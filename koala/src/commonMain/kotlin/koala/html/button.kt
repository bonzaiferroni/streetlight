package koala.html

import koala.css.CssClass
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.*
import kotlinx.html.button as buttonCore

fun FlowOrInteractiveOrPhrasingContent.button(
    text: String,
    onClick: String? = null,
    modifiers: ModifierSet? = null,
    content: BUTTON.() -> Unit = {},
) {
    buttonCore {
        applyModifiers(modify(ElementClass.button, modifiers))
        onClick?.let { this.onClick = it }
        +text
        content()
    }
}