package koala.dom

import koala.css.ModifierSet
import koala.css.Row
import koala.css.setModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun DOMContext.row(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = row(modifiers) {
    this.id = id.value
    content()
}

inline fun DOMContext.row(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    setModifiers(Row, modifiers)
    content()
}