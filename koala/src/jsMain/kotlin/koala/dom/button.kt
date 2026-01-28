package koala.dom

import kotlinx.html.js.button
import kotlinx.html.js.onClickFunction
import org.w3c.dom.events.Event

fun DOMContext.button(text: String, onClick: (Event) -> Unit) = button {
    +text
    onClickFunction = onClick
}