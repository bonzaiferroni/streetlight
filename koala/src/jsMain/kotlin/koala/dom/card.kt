package koala.dom

import koala.css.Card
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.DIV
import kotlinx.html.js.div

inline fun DOMContext.card(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    applyModifiers(Card, modifiers)
    content()
}