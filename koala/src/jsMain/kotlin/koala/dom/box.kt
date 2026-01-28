package koala.dom

import koala.css.Box
import koala.css.CssClass
import koala.css.modify
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun DOMContext.box(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) = div {
    modify(Box, *modifiers)
    content()
}

inline fun DOMContext.box(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit = { },
) = box(modifiers = modifiers) {
    this.id = id.value
    content()
}