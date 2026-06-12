package koala.dom

import koala.css.Box
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun TagScope.box(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = div {
    addModifiers(Box, modifiers)
    content()
}

inline fun TagScope.box(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = box(modifiers = modifiers) {
    this.id = id.identifier
    content()
}

fun TagScope.container(
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = div {
    addModifiers(modifiers)
    content()
}

fun TagScope.container(
    id: Id,
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = container(modifiers = modifiers) {
    this.id = id.identifier
    content()
}