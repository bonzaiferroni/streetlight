package koala.html

import koala.css.ElementClass
import koala.css.ModifierSet
import koala.css.applyModifiers
import koala.css.modify
import kotlinx.html.A
import kotlinx.html.FlowOrInteractiveOrPhrasingContent
import kotlinx.html.a

fun FlowOrInteractiveOrPhrasingContent.action(
    route: AppRoute,
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    action(href = route.toHashPath(), modifiers = modifiers, block = block)
}

fun FlowOrInteractiveOrPhrasingContent.action(
    href: String? = null,
    text: String = "",
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    action(modifiers) {
        href?.let { this.href = href }
        +text
        block?.invoke(this)
    }
}

fun FlowOrInteractiveOrPhrasingContent.action(
    modifiers: ModifierSet? = null,
    block: (A.() -> Unit)? = null
) {
    a {
        applyModifiers(modify(ElementClass.action, modifiers))
        block?.invoke(this)
    }
}