package koala.dom

import koala.css.Card
import koala.css.CssClass
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.js.div

inline fun DOMContext.card(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) = div {
    modify(Card, *modifiers)
    content()
}