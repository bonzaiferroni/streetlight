package koala.dom

import koala.css.Box
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun DOMContext.box(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = div {
    addModifiers(Box, modifiers)
    content()
}

inline fun DOMContext.box(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = box(modifiers = modifiers) {
    this.id = id.identifier
    content()
}

fun DOMContext.container(
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = div {
    addModifiers(modifiers)
    content()
}

fun DOMContext.container(
    id: Id,
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = container(modifiers = modifiers) {
    this.id = id.identifier
    content()
}