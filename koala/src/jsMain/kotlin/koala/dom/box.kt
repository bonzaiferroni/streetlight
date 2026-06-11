package koala.dom

import koala.css.Box
import koala.css.ModifierSet
import koala.css.addModifiers
import koala.html.Id
import kotlinx.html.DIV
import kotlinx.html.js.div
import kotlinx.html.id

inline fun DOM.box(
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = div {
    addModifiers(Box, modifiers)
    content()
}

inline fun DOM.box(
    id: Id,
    modifiers: ModifierSet? = null,
    crossinline content: DIV.() -> Unit = { },
) = box(modifiers = modifiers) {
    this.id = id.identifier
    content()
}

fun DOM.container(
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = div {
    addModifiers(modifiers)
    content()
}

fun DOM.container(
    id: Id,
    modifiers: ModifierSet? = null,
    content: DIV.() -> Unit = { },
) = container(modifiers = modifiers) {
    this.id = id.identifier
    content()
}