package koala.dom

import koala.css.Column
import koala.css.CssClass
import koala.css.modify
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.column(
    id: Id,
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
) = column(*modifiers) {
    this.id = id.value
    content()
}

inline fun DOMContext.column(
    vararg modifiers: CssClass,
    crossinline content: DIV.() -> Unit,
): HTMLDivElement {
    return div {
        modify(Column, *modifiers)
        content()
        div { }
    }
}