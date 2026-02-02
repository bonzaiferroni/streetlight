package koala.html

import koala.css.CssClass
import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowContent
import kotlinx.html.a

fun FlowContent.action(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    action(href = route.toHashPath(), modifiers = modifiers, block = block)
}

fun FlowContent.action(
    href: String? = null,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    action(modifiers) {
        href?.let { this.href = href }
        block?.invoke(this)
    }
}

fun FlowContent.action(
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    a {
        applyModifiers(modify(ElementClass.action, modifiers))
        block?.invoke(this)
    }
}