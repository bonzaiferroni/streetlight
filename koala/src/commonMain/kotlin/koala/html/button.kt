package koala.html

import koala.css.ModifierSet
import koala.css.addModifiers
import kotlinx.html.BUTTON
import kotlinx.html.FlowContent
import kotlinx.html.button

fun FlowContent.button(
    text: String,
    modifiers: ModifierSet? = null,
    block: (BUTTON.() -> Unit)? = null,
) {
    button {
        addModifiers(BtnKey.Class, modifiers)
        block?.invoke(this)
        +text
    }
}