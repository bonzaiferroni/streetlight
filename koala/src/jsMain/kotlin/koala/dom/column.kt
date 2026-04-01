package koala.dom

import koala.css.Column
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.css.modify
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id
import org.w3c.dom.HTMLDivElement

inline fun DOMContext.column(
    id: Id?,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = column(modifiers) {
    id?.let {
        this.id = id.identifier
    }
    content()
}

inline fun DOMContext.column(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
): HTMLDivElement {
    return div {
        addModifiers(modify(Column, modifiers))
        content()
    }
}