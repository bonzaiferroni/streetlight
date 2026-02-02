package koala.html

import koala.css.Card
import koala.css.CssClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import kotlinx.html.DIV

import kotlinx.html.FlowContent
import kotlinx.html.div

inline fun FlowContent.card(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) {
    div {
        applyModifiers(Card, modifiers)
        content()
    }
}