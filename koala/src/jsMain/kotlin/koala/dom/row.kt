package koala.dom

import koala.css.ModifierSet
import koala.css.Row
import koala.css.addModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun TagScope.row(
    id: Id,
    mod: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = row(mod) {
    this.id = id.identifier
    content()
}

inline fun TagScope.row(
    mod: ModifierSet? = null,
    crossinline content: DIV.() -> Unit,
) = div {
    addModifiers(Row, mod)
    content()
}