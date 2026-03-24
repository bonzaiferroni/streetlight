package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.modify
import koala.css.setModifiers
import kotlinx.html.A
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button

fun FlowContent.button(
    text: String,
    modifiers: ModifierSet? = null,
    block: (BUTTON.() -> Unit)? = null,
) {
    button {
        setModifiers(ElementClass.button, modifiers)
        block?.invoke(this)
        +text
    }
}