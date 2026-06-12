package koala.dom

import koala.css.ModifierSet
import koala.css.Row
import koala.css.addModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun AppendScope.row(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = row(modifiers) {
    this.id = id.identifier
    content()
}

inline fun AppendScope.row(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    addModifiers(Row, modifiers)
    content()
}