package koala.dom

import koala.css.CssClass
import koala.css.ModifierSet
import koala.css.Row
import koala.css.applyModifiers
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
    applyModifiers(Row, modifiers)
    content()
}