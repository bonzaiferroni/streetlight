package koala.dom.layout

import koala.modifier.*
import koala.page.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id
import koala.dom.AppendScope
import koala.dom.asWeb

/** A container whose children are stacked in one grid cell, over one another. */
inline fun AppendScope.box(
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit = { },
) = div {
    addModifiers(Box, mod)
    content()
}.asWeb()

/** A [box] with [id]. */
inline fun AppendScope.box(
    id: Id,
    mod: Modifier? = null,
    crossinline content: DIV.() -> Unit = { },
) = box(mod = mod) {
    this.id = id.identifier
    content()
}