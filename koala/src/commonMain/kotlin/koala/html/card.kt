package koala.html

import koala.css.Card
import koala.css.CssClass
import koala.css.modify
import kotlinx.html.DIV

import kotlinx.html.FlowContent
import kotlinx.html.TagConsumer
import kotlinx.html.div

inline fun FlowContent.card(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) {
    div {
        modify(Card, *modifiers)
        content()
    }
}