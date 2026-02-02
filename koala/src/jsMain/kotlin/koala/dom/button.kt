package koala.dom

import koala.css.ButtonClass
import koala.css.CssClass
import koala.css.applyModifiers
import kotlinx.html.js.button
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event

fun DOMContext.button(
    text: String,
    vararg modifiers: CssClass,
    onClick: ((Event) -> Unit)? = null
) = button {
    applyModifiers(ButtonClass, *modifiers)
    +text
    onClick?.let {
        onClickFunction = it
    }
}