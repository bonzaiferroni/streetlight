package koala.html

import koala.css.Card
import koala.css.CssClass
import koala.css.applyModifiers
import kotlinx.html.DIV

import kotlinx.html.FlowContent
import kotlinx.html.div

inline fun FlowContent.card(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        applyModifiers(Card, *modifiers)
        content()
    }
}