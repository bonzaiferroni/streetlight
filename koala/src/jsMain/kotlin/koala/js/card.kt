package koala.js

import koala.css.Card
import koala.css.CssClass
import koala.css.modify
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.js.div
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement

inline fun <C: HTMLElement> TagConsumer<C>.card(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) = div {
    modify(Card, *modifiers)
    content()
}