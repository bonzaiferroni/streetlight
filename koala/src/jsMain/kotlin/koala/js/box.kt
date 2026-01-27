package koala.js

import koala.css.Box
import koala.css.CssClass
import koala.css.modify
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.js.div
import kotlinx.html.id
import org.w3c.dom.HTMLElement

inline fun <C: HTMLElement> TagConsumer<C>.box(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) = div {
    modify(Box, *modifiers)
    content()
}

inline fun <C: HTMLElement> TagConsumer<C>.box(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) = box(modifiers = modifiers) {
    this.id = id.value
    content()
}