package koala.dom

import koala.css.*
import koala.html.AppRoute
import kotlinx.html.A
import kotlinx.html.a
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.events.Event

fun RenderContext.action(
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = a {
    applyModifiers(ElementClass.action, modifiers)
    block?.invoke(this)
}

fun RenderContext.action(
    onClick: ((Event) -> Unit)? = null,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = action(modifiers = modifiers) {
    onClick?.let {
        onClickFunction = onClick
    }
    block?.invoke(this)
}

fun RenderContext.action(
    href: String? = null,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = action(modifiers = modifiers) {
    href?.let { this.href = href }
    block?.invoke(this)
}

fun RenderContext.action(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) = action(modifiers = modifiers) {
    href = route.toHashPath()
    block?.invoke(this)
}