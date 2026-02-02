package koala.dom

import koala.css.Column
import koala.css.CssClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.column(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = column(modifiers) {
    this.id = id.value
    content()
}

inline fun DOMContext.column(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
): HTMLDivElement {
    return div {
        applyModifiers(modify(Column, modifiers))
        content()
    }
}